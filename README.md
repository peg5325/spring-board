# 🌟 Spring Board - 게시판 서비스

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20S3%20%7C%20Route53-yellow.svg)](https://aws.amazon.com/)

Spring Boot를 기반으로 한 현대적인 게시판 웹 애플리케이션입니다. 카카오 소셜 로그인, 파일 업로드, 해시태그 기능 등을 제공합니다.

## 📋 목차

- [주요 기능](#-주요-기능)
- [시스템 아키텍처](#-시스템-아키텍처)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [API 문서](#-api-문서)
- [데이터베이스 설계](#-데이터베이스-설계)


## ✨ 주요 기능

### 🔐 인증 및 권한
- **카카오 소셜 로그인**: OAuth2를 통한 간편 로그인
- **세션 기반 인증**: Spring Security를 활용한 보안 관리

### 📝 게시글 관리
- **CRUD 기능**: 게시글 작성, 조회, 수정, 삭제
- **검색 기능**: 제목, 본문, 작성자, 해시태그별 검색
- **정렬 기능**: 작성일, 제목 등 다양한 기준으로 정렬
- **페이징**: 효율적인 데이터 로딩을 위한 페이지네이션

### 🏷️ 해시태그 시스템
- **해시태그 등록**: 게시글에 다중 해시태그 추가
- **해시태그 검색**: 특정 해시태그로 게시글 필터링
- **해시태그 관리**: 자동 중복 제거 및 정규화

### 💬 댓글 시스템
- **댓글 작성**: 게시글에 댓글 추가
- **대댓글 지원**: 계층형 댓글 구조
- **댓글 관리**: 수정, 삭제 기능

### 📎 파일 관리
- **파일 업로드**: AWS S3를 통한 안전한 파일 저장
- **이미지 미리보기**: 업로드된 이미지 썸네일 표시
- **파일 다운로드**: 원본 파일명으로 다운로드
- **파일 검증**: 파일 타입 및 크기 제한

## 🏗️ 시스템 아키텍처

![System Architecture](document/system-architecture.svg)

### 📋 아키텍처 구성요소 설명

| 구성요소 | 역할 | 설명 |
|---------|------|------|
| **사용자** | 서비스 이용 | 웹 브라우저를 통해 게시판 서비스 접근 |
| **Route53** | DNS 서비스 | 도메인 이름을 실제 서버 주소로 변환 |
| **Load Balancer** | 트래픽 분산 | 사용자 요청을 서버로 안전하게 전달 |
| **EC2 Instance** | 서버 호스팅 | Spring Boot 애플리케이션이 실행되는 가상 서버 |
| **Spring Boot App** | 핵심 애플리케이션 | 게시판의 모든 비즈니스 로직 처리 |
| **MySQL** | 데이터베이스 | 게시글, 사용자, 댓글 등 모든 데이터 저장 |
| **AWS S3** | 파일 저장소 | 이미지, 첨부파일 등 정적 자원 저장 |
| **Kakao OAuth** | 소셜 로그인 | 카카오 계정을 통한 사용자 인증 |

## 🛠️ 기술 스택

### Backend
- **Framework**: Spring Boot 2.7.0
- **Language**: Java 17
- **Build Tool**: Gradle 8.5
- **Database**: MySQL 8.0, H2 (테스트)
- **ORM**: Spring Data JPA, QueryDSL 5.0.0
- **Security**: Spring Security, OAuth2 Client
- **Template Engine**: Thymeleaf

### Frontend
- **CSS Framework**: Bootstrap 5.2.0-Beta1
- **JavaScript**: Vanilla JS
- **Icons**: Bootstrap Icons

### Infrastructure & DevOps
- **Cloud**: AWS (EC2, S3, Route53)
- **CI/CD**: GitHub Actions
- **Monitoring**: Spring Boot Actuator

### Development Tools
- **IDE**: IntelliJ IDEA Ultimate
- **Version Control**: Git, GitHub
- **API Testing**: Postman
- **Database Tool**: MySQL Workbench

## 📁 프로젝트 구조

```
spring-board/
├── 📁 src/main/java/com/springboard/projectboard/
│   ├── 📁 config/          # 설정 클래스
│   ├── 📁 controller/      # 웹 컨트롤러
│   ├── 📁 domain/          # 엔티티 클래스
│   ├── 📁 dto/             # 데이터 전송 객체
│   ├── 📁 repository/      # 데이터 접근 계층
│   └── 📁 service/         # 비즈니스 로직
├── 📁 src/main/resources/
│   ├── 📁 static/          # 정적 리소스 (CSS, JS, 이미지)
│   ├── 📁 templates/       # Thymeleaf 템플릿
│   └── 📄 application.yml  # 애플리케이션 설정
├── 📁 document/            # 프로젝트 문서
│   ├── 📄 use-case.svg     # 유즈케이스 다이어그램
│   └── 📄 project-board-erd.svg # ERD 다이어그램
└── 📁 .github/workflows/   # GitHub Actions 워크플로우
```

## 📚 API 문서

### 게시글 API
- `GET /articles` - 게시글 목록 조회 (검색, 정렬, 페이징 지원)
- `GET /articles/{articleId}` - 게시글 상세 조회
- `GET /articles/search-hashtag` - 해시태그별 게시글 검색
- `GET /articles/form` - 게시글 작성 폼
- `POST /articles/form` - 게시글 작성 (파일 업로드 지원)
- `GET /articles/{articleId}/form` - 게시글 수정 폼
- `POST /articles/{articleId}/form` - 게시글 수정 (파일 업로드 지원)
- `POST /articles/{articleId}/delete` - 게시글 삭제

### 댓글 API
- `POST /comments/new` - 댓글 작성
- `POST /comments/{commentId}/delete` - 댓글 삭제

### 파일 API
- `GET /files/download/{fileId}` - 파일 다운로드
- `DELETE /files/{fileId}` - 파일 삭제

자세한 API 문서는 [Google Sheets](https://docs.google.com/spreadsheets/d/1QZbxWXE8eDl_KJqXE8DoUvMlRHGoocLxI7Y5rKXrPRE/edit?gid=0#gid=0)에서 확인할 수 있습니다.

## 🗄️ 데이터베이스 설계

### ERD 다이어그램
![ERD](document/project-board-erd.svg)

### 주요 테이블
- **user_account**: 사용자 정보
- **article**: 게시글 정보
- **article_comment**: 댓글 정보
- **hashtag**: 해시태그 정보
- **article_hashtag**: 게시글-해시태그 매핑
- **article_file**: 첨부파일 정보

## 🎯 유즈케이스

![Use Case](document/use-case.svg)

## 📞 문의사항

프로젝트에 관한 질문이나 제안사항이 있으시면 다음을 이용해 주세요:

- **Issues**: [GitHub Issues](https://github.com/peg5325/spring-board/issues)
- **Email**: peg5325@gmail.com
- **Blog**: [개발 블로그](https://abcdeongq.tistory.com/)

---

⭐ 이 프로젝트가 도움이 되었다면 Star를 눌러주세요!