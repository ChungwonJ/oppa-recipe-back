# 🍳 Oppa Recipe Backend

> **Gemini AI + YouTube API 기반 음식 사진 레시피 추출 서비스**

사용자가 음식 사진을 업로드하면 AI(Gemini API)가 음식을 인식하고, YouTube API를 통해 해당 음식의 레시피 쇼츠 영상을 제공하는 백엔드 서비스입니다.

Spring Boot 기반 REST API 서버로 구현되었습니다.

---

# 📌 Project Overview

요리를 하고 싶지만 레시피를 찾기 어려운 문제를 해결하기 위해  
**음식 사진만으로 레시피를 찾을 수 있는 서비스**를 구현했습니다.

서비스 동작 방식

1️⃣ 사용자가 음식 사진을 업로드합니다.

2️⃣ **Gemini API**가 이미지를 분석하여 음식 이름을 추출합니다.

3️⃣ 추출된 음식 이름을 기반으로 **YouTube Data API**에서 레시피 영상을 검색합니다.

4️⃣ 레시피 쇼츠 영상 URL을 반환합니다.

---
# 🧑‍💻 My Role

### Backend Developer (개인 프로젝트)

- Spring Boot 기반 REST API 서버 개발
- Gemini API 이미지 분석 기능 구현
- YouTube Data API 레시피 검색 기능 구현
- JWT 인증 시스템 구축
- OAuth 네이버 로그인 구현
- AWS 배포 환경 구축

---

# 🛠 Tech Stack

## Backend

- Java
- Spring Boot
- Spring Security
- Spring Data JPA

## AI

- Gemini API (이미지 음식 인식)

## External API

- YouTube Data API

## Database

- MySQL

## Authentication

- OAuth2 (Naver Login)
- JWT (Access / Refresh Token)

## Infrastructure

- AWS Elastic Beanstalk
- AWS RDS

---