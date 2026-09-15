import SwiftUI
import Translation
import Shared

// DRAFT -- written without an Xcode/iOS toolchain, verify on a real device.
//
// Bridges Apple's on-device Translation framework into the shared Kotlin
// `Translator`. `ContentView` creates one of these, attaches
// `.translationBridge(_:)`, and registers it on `IosTranslatorHolder` so the
// common code (HomeViewModel -> Translator) can call into it.
//
// Known limitations of this draft:
//  - One in-flight job at a time. HomeViewModel translates once per feed load,
//    so that's enough; a second call while one is running drops the first.
//  - Source language is fixed to English, target to Korean.
//  - The Translation framework does not run on the Simulator.
//  - First run shows a system prompt to download the language models; that UI
//    is handled by `.translationTask`.
//  - `Locale.Language(identifier:)` values ("en" / "ko") may need to be region
//    qualified ("en-US") -- confirm against the installed models.

@MainActor
final class AppleTranslator: ObservableObject, IosTranslatorBackend {

    fileprivate struct Job: Equatable {
        let id = UUID()
        let texts: [String]
        static func == (lhs: Job, rhs: Job) -> Bool { lhs.id == rhs.id }
    }

    @Published fileprivate var job: Job?
    private var pendingCompletion: (([String]) -> Void)?

    // IosTranslatorBackend. Called from Kotlin (Dispatchers.Main).
    nonisolated func translate(texts: [String], onResult: @escaping ([String]) -> Void) {
        Task { @MainActor in
            pendingCompletion?(job?.texts ?? [])   // abandon any previous job
            pendingCompletion = onResult
            job = Job(texts: texts)
        }
    }

    fileprivate func complete(_ result: [String]) {
        pendingCompletion?(result)
        pendingCompletion = nil
        job = nil
    }
}

private struct TranslationBridge: ViewModifier {
    @ObservedObject var translator: AppleTranslator
    @State private var configuration: TranslationSession.Configuration?

    func body(content: Content) -> some View {
        content
            .onChange(of: translator.job) { _, job in
                guard job != nil else { return }
                if configuration == nil {
                    configuration = TranslationSession.Configuration(
                        source: Locale.Language(identifier: "en"),
                        target: Locale.Language(identifier: "ko")
                    )
                } else {
                    configuration?.invalidate()
                }
            }
            .translationTask(configuration) { session in
                guard let job = translator.job else { return }
                let requests = job.texts.enumerated().map { index, text in
                    TranslationSession.Request(sourceText: text, clientIdentifier: String(index))
                }
                do {
                    let responses = try await session.translations(from: requests)
                    var byIndex: [Int: String] = [:]
                    for response in responses {
                        guard let clientIdentifier = response.clientIdentifier,
                              let index = Int(clientIdentifier) else { continue }
                        byIndex[index] = response.targetText
                    }
                    let translated = job.texts.enumerated().map { index, original in
                        byIndex[index] ?? original
                    }
                    translator.complete(translated)
                } catch {
                    translator.complete(job.texts)   // originals on failure
                }
            }
    }
}

extension View {
    func translationBridge(_ translator: AppleTranslator) -> some View {
        modifier(TranslationBridge(translator: translator))
    }
}
