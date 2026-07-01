# Spring Study

김영한 스프링 입문 강의를 따라가며 만든 실습 코드와 학습 기록을 관리하는 저장소입니다.

## 구성

- `hello-spring/`: Spring Boot 실습 프로젝트
- `issues/`: Codex에 물어본 내용과 학습 정리를 이슈 단위로 기록하는 Markdown 원본
- `.github/learning-plan.json`: GitHub milestone/issue/label 자동 생성 설정
- `scripts/sync-github-learning.ps1`: 이슈 동기화, 커밋, 브랜치 push, PR 생성을 한 번에 처리하는 스크립트

## 학습 기록 흐름

1. Codex에 질문하거나 새로 배운 내용을 `issues/NNNN-topic.md`로 정리합니다.
2. Markdown 파일을 최종 학습 기록으로 사용합니다. 별도 HTML 산출물은 만들지 않습니다.
3. 관련 챕터가 여러 개면 챕터별로 커밋을 나누고, 하나의 PR로 묶습니다.
4. GitHub에 올릴 준비가 되면 `scripts/sync-github-learning.ps1`을 실행합니다.
5. 스크립트가 label, milestone, issue, branch, 챕터별 commit, PR을 만들고 PR 링크를 출력합니다.
6. PR이 병합된 뒤에는 아래 **브랜치 정리** 절차를 따릅니다.

## PR 병합 후 브랜치 정리

PR이 main에 병합되면 원격 브랜치와 로컬 브랜치를 모두 삭제합니다.

```powershell
# 1. main으로 전환하고 최신 상태로 동기화
git switch main
git pull origin main

# 2. 병합된 원격 브랜치 삭제
git push origin --delete <브랜치명>   # 예: learning/20260629-195054

# 3. 로컬 브랜치 삭제
git branch -d <브랜치명>

# 4. 원격에서 이미 삭제된 추적 브랜치 정리 (옵션)
git fetch --prune
```

> GitHub 웹 UI에서 PR 병합 직후 **"Delete branch"** 버튼을 누르면 원격 브랜치는 자동 삭제됩니다.
> 로컬에서는 `git fetch --prune` 후 `git branch -d <브랜치명>` 으로 마무리합니다.

## GitHub 자동 발행

GitHub 원격 저장소를 연결하고 `gh` 로그인이 끝난 뒤 실행합니다.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\sync-github-learning.ps1
```

이 스크립트가 처리하는 일:

- `.github/learning-plan.json` 기준으로 label 생성
- milestone 생성
- `issues/*.md`를 GitHub issue로 생성
- `issues/NNNN-*.md` 변경사항을 챕터 번호별 커밋으로 분리
- README, 템플릿, 스크립트 같은 학습 워크플로 변경사항은 별도 커밋으로 분리
- 앱 코드 변경사항이 섞여 있으면 자동 커밋하지 않고 중단
- `learning/yyyyMMdd-HHmmss` 브랜치 push
- PR 생성 후 링크 출력

강의자료 PDF는 루트 `.gitignore`에서 제외되어 Git에 올라가지 않습니다.
