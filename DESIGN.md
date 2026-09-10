# AI Short Cut 디자인 시스템 (Design System)

## 1. 디자인 원칙
- 정보 밀도: 구조화된 카드/리스트로 기술 데이터를 압축 표현
- 다크 테크 에스테틱: 딥 네이비 배경 + 일렉트릭 블루/퍼플 포인트
- 절제된 깊이감: 그림자 대신 1dp 보더로 레이어 구분 (글로우 효과 미사용)

## 2. 컬러 팔레트 → Material3 ColorScheme 매핑
| Token | Hex | Compose ColorScheme role |
|---|---|---|
| Background | #0B0D14 | background |
| Surface | #151927 | surface |
| Surface Bright | #353948 | surfaceVariant |
| Primary | #7C3AED | primary |
| Secondary | #3B82F6 | secondary |
| On Surface | #FFFFFF | onSurface |
| On Surface Variant | #94A3B8 | onSurfaceVariant |

Gradient(#3B82F6 → #7C3AED)는 ColorScheme 밖 커스텀 브랜드 토큰으로 별도 관리 (예: `LocalBrandGradient`).

## 3. 스페이싱 스케일 (4dp 기준 그리드)
| Token | 값 |
|---|---|
| space-xs | 4dp |
| space-sm | 8dp |
| space-md | 16dp |
| space-lg | 24dp |
| space-xl | 32dp |

- 카드 내부 패딩: space-md
- 카드 간 간격: space-sm
- 화면 좌우 마진: space-md
- 섹션 간 간격: space-lg

## 4. 타이포그래피 스케일
| Style | Size / Weight | 용도 |
|---|---|---|
| Headline | 22sp / SemiBold | 화면 타이틀 |
| Title | 18sp / SemiBold | 카드 헤더 |
| Body | 14sp / Regular | 본문 |
| Label | 12sp / Medium | 캡션, 메타데이터 |
| Data | 13sp / Medium, tabular-nums | 벤치마크 수치 |

Font: Pretendard (fallback: 시스템 산세리프)

## 5. 컴포넌트 규칙
- Corner radius: 16dp (카드/버튼/검색 필드 공통)
- Elevation: 그림자 없음, `1dp #353948` border로 대체
- Bottom Nav: 3탭(Home/Discover/Saved), 아이콘+라벨
- Chip: outline 기본, 선택 시 fill (#7C3AED)
- 상태: pressed는 surface 대비 8% 밝기, disabled는 opacity 38%

### 공용 컴포넌트 (`core:designsystem`)
- `ScreenHeader(title, trailing)` — 화면 최상단 타이틀(Headline) + 그 아래 28×3dp 브랜드 그라디언트 규칙. 브랜드 그라디언트가 등장하는 유일한 상시 위치이다. `trailing` 슬롯에 화면별 액션(언어 토글 등)을 건다.
- `SectionHeader(text)` — 카드 묶음 위의 구획 라벨. Title 크기에 `onSurfaceVariant` 색, 위쪽 space-lg 간격.
- `StatLine(parts, color)` — 숫자 메타데이터 한 줄. "Data" 타입 스타일(13sp, tabular), 가운뎃점 구분. OpenRouter 보강 값은 `primary` 색으로 구분한다.
- `FeedCard` — 위 규칙(16dp, 1dp 보더, space-md 패딩)을 적용한 카드.

## 6. 아이콘
- Outlined Material Icons, Regular weight
- 텍스트 라벨과 병행 사용 (아이콘 단독 사용 지양)
