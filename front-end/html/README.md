# 1. HTML

브라우저는 HTML 문서를 응답받아서 화면에 그린다. (렌더링)

```html
<form action="./article" method="post">
	<input type="text" name="title">
	<input type="text" name="content">
	<input type="submit">
</form>
```

- `form` - 이름
- `action` , `method` - 속성 (Attributes)
- `"./article"`, `"post"` - 값
- 속성과 값에 의해 TAG의 구체적인 행동이 결정된다!

## 1) 개요

### 버전

`<!DOCTYPE html>`: 현대 웹에서 가장 일반적으로 사용되는 html 5 버전

### 최상단
`<html>`
`</html>`

## 2) 내부

### `<head>`

메타데이터를 넣는 곳이다.

- **주요 테그**
    - `<title>`
        - 웹 브라우저의 가장 위의 탭 쪽에서 표현되는 내용
    - `<meta>`
        - 인코딩
            - `<meta charset="UTF-8">`
            - 파일의 실제 저장방식과 일치하도록 해야 글씨가 깨지지 않음
        - 검색 엔진을 위한 페이지 설명 등

          ![image](https://github.com/user-attachments/assets/22300313-7f76-4bac-a1c0-2beddb5c4540)

    - `<link>`
        - css 파일을 해당 웹 페이지에 적용하기 위해서 사용

            ```html
            <link rel="stylesheet" href="style.css">
            
            안녕하세요 <span class="custom-style">개발자 윤지용</span> 입니다.
            ```
            
            ```html
            .custom-style {
              font-size: 30px;
            }
            ```

            ![image](https://github.com/user-attachments/assets/47b8faac-85b0-4a72-81f9-5aa1fad0c262)
            
            - 개발자 도구에서 보면 `index.html`을 최초에 요청하고, 그 안에 있는 link로 `style.css`를 추가로 요청하고 있음

          ![image](https://github.com/user-attachments/assets/8047ebe5-e747-4039-90f1-4e03781f231b)

            - 재요청 시 웹 브라우저가 가지고 있는 캐시를 리턴하고 있어서 `status`가 `304`가 리턴되고 있음

### `<body>`

ui 실제 구성 내용을 넣는 곳이다.

- **주요 테그**
    - `<a>`
        - 다른 페이지로 연결하는 링크 생성
        - `<a *href*="https://www.inflearn.com/">인프런 링크</a>`
        - `<a *href*="https://www.inflearn.com/" *target*="_blank">인프런 링크</a>` - target → 새 탭으로
    - `<img>`
        - 웹 페이지에 이미지를 보여줌
        - `<img *src*="https://cdn.inflearn.com/assets/brand/logo.png">`
        - `resources` 디렉토리에 이미지를 직접 넣어서 불러올 수도 있음
    - `<script>`
        - 웹 페이지에 javascript 코드를 넣을 수 있음

### `주석`

- `<!— 주석 —>`

---

- **테스트 전체 코드**

    ```html
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="UTF-8">
      <title>테스트타이틀</title>
      <link rel="stylesheet" href="style.css">
    </head>
    <body>
      안녕하세요 <span class="custom-style">개발자 윤지용</span> 입니다.
      <a href="https://www.inflearn.com/" target="_blank">인프런 링크</a>
      <img src="https://cdn.inflearn.com/assets/brand/logo.png">
      <img src="channels4_profile.jpg">
    
      <!-- 개발자 도구 콘솔에서 볼 수 있음 -->
      <script>
        console.log('로그 찍기');
        alert("html 공부중!!!")
      </script>
    </body>
    </html>
    ```

- **프로젝트의 resources 구조**

  ![image.png](https://github.com/user-attachments/assets/ae1cc7d3-1e20-43a5-b472-c20afac7a0f0)