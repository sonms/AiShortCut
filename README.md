# AiShortCut

AI 관련 뉴스, 모델, 저장소의 흐름을 한 곳에 모아 매일 훑어볼 수 있는 KMP(Kotlin Multiplatform) 앱입니다. Android와 iOS를 하나의 코드베이스로 구현합니다.

## 왜 만들었나

X, 커뮤니티 등 SNS 피드는 정보가 스쳐 지나가고 하루 지나면 다시 찾기 어렵습니다. AI 쪽은 특히 변화 속도가 빨라서, 모델 릴리스·논문·트렌딩 레포를 훑어보는 습관을 SNS 스크롤에 의존하고 싶지 않았습니다. 이 앱은 "오늘 뭐가 나왔는지"를 몇 개의 무료/무인증 공개 API에서 긁어와 계속 다시 볼 수 있는 형태(북마크 가능한 피드)로 모아두는 것이 목적입니다.

## 화면 구성

Material 3 bottom navigation, 3탭:

- **Home** — Hugging Face 데일리 논문 + arXiv 최신 글을 합친 피드, 트렌딩 키워드 칩, GitHub 트렌딩 레포 일부.
- **Discover** — Hugging Face 트렌딩 모델 목록에 OpenRouter 가격/벤치마크 지표(있는 경우)를 붙여서 보여주고, GitHub 트렌딩 레포도 함께 검색/필터.
- **Saved** — Room으로 로컬에 저장한 북마크(뉴스 아티클) 목록.

## API 통합 (정확하게)

전부 인증 없이 쓸 수 있는 공개 엔드포인트만 사용합니다. API 키나 시크릿은 없습니다.

| 소스 | 엔드포인트 | 용도 / 비고 |
|---|---|---|
| Hugging Face Trending | `GET huggingface.co/api/trending?type=model&limit=` | 트렌딩 모델 목록 (`data:hftrending`) |
| Hugging Face Daily Papers | `GET huggingface.co/api/daily_papers?limit=30` | 사람이 큐레이션한 데일리 논문 — Home 피드의 메인 소스 (`data:newsfeed`) |
| arXiv Atom API | `GET export.arxiv.org/api/query?search_query=cat:cs.AI&sortBy=submittedDate...` | 데일리 페이퍼 뒤에 붙는 보조 피드. Ktor로 받은 XML을 자체 정규식 파서(`FeedParser`)로 파싱 — RSS 2.0/Atom 태그만 다루는 미니멀 구현이라 제대로 된 XML 파서는 아님 |
| GitHub Search API | `GET api.github.com/search/repositories?q=topic:artificial-intelligence+created:>{30일전}&sort=stars` | GitHub엔 별도 trending 엔드포인트가 없어서, "최근 생성 + AI 토픽 + 스타순 정렬"로 근사. 비인증 상태라 분당 10회 제한 |
| OpenRouter Models | `GET openrouter.ai/api/v1/models` | 모델별 가격 및 Artificial Analysis 벤치마크(intelligence/coding/agentic index). `hugging_face_id`로 HF 트렌딩 모델과 조인, 못 찾으면 그 모델은 그냥 가격/벤치마크 없이 표시 |

한 소스가 실패해도(레이트리밋, 타임아웃 등) 나머지 소스는 그대로 보여줍니다 — `NewsFeedRepository`가 소스별로 try/catch해서 실패한 피드만 빈 리스트로 취급합니다.

## 기술 스택

- **Kotlin Multiplatform + Compose Multiplatform** — Android/iOS 공용 UI·로직. 모듈은 `core:*` / `data:*` / `presentation:*` 세 계층으로 나뉘고, 각각 전용 Gradle convention plugin(`build-logic/convention`)을 적용합니다 (feature 모듈에 `com.android.library`를 직접 붙이지 않음).
- **Ktor** — 네트워킹. Android는 OkHttp 엔진, iOS는 Darwin 엔진 (`core:network`).
- **kotlinx.serialization** — API 응답 DTO 파싱.
- **Room (KMP)** — 저장된 아티클의 로컬 영속화 (`core:database`, `data:saved`).
- **Koin** — DI (`presentation:main`의 `AppModule`에서 전체 그래프 조립).
- **Kermit** — 로깅 (네트워크 로그도 여기로 라우팅).
- **번역** — `core:translate`가 피드 텍스트를 한국어로 번역하는 `Translator` 인터페이스를 정의. Android는 ML Kit Translate로 구현되어 있고, iOS는 Swift 쪽에서 Apple Translation 프레임워크를 붙이는 콜백 훅(`IosTranslatorBackend`)만 준비된 상태 — 아직 미연동이면 원문을 그대로 통과시킵니다.

## 빌드

```bash
# Android
./gradlew :androidApp:assembleDebug

# 특정 모듈만
./gradlew :layer:name:build
```

iOS는 `/iosApp`을 Xcode에서 열어서 실행합니다 (이 저장소 개발은 Windows/기타 환경에서 진행되어 iOS 빌드는 이 머신에서 직접 검증하지 못했습니다 — 로직은 `commonMain`에 두고 컴파일 확인까지만).

## TODO / 미완성

- **로컬 저장소 마이그레이션**: `data:home`의 `HomeLocalStore`는 `expect`/`actual`로 손으로 짠 SharedPreferences/NSUserDefaults 래퍼입니다. Room으로 옮겨가는 게 결정된 방향이고, 이 패턴을 새 `data:*` 모듈에 그대로 복사하면 안 됩니다.
- **벤치마크 차트**: Discover 탭은 지금 벤치마크 수치를 텍스트(`StatLine`)로만 보여줍니다. MMLU/HumanEval류를 그래프로 그리는 건 아직 안 붙였고, Canvas API로 직접 그릴지 KoalaPlot 같은 KMP 차트 라이브러리를 쓸지도 미정입니다.
- **GitHub API 레이트리밋**: 비인증 10 req/min이라 사용량이 늘면 곧 막힙니다. 인증 토큰 붙이는 문제는 아직 손대지 않았습니다.
- **RSS 파서의 한계**: `FeedParser`는 정규식 기반이라 CDATA/기본 XML 엔티티 정도만 처리합니다. 이 두 파서가 못 다루는 형식의 피드가 새로 추가되면 깨질 수 있습니다.
- **iOS 번역 미연동**: Apple Translation 프레임워크 쪽 Swift 구현체가 아직 없어서, iOS에서는 번역이 항상 원문 통과입니다.
- **iOS 빌드 미검증**: 위에서 언급한 대로, iOS 타겟은 컴파일만 확인했고 실제 기기/시뮬레이터 실행 검증은 못 했습니다.
