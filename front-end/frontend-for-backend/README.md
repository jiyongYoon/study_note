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

# 2. XHR과 AJAX

- XHR: XML Http Request
- AJAX: Asynchronous JavaScript and XML
- 그러나 요즘은 xml이 아닌 json을 주고 받는 형태로 거의 변화했다.

## 실습 1

- Controller.java

    ```java
    @RestController
    public class NoParameterAjaxRestController {
    
        @RequestMapping("/get-with-no-parameter")
        public String getWithNoParameter() {
            return "파라미터가 없는 GET 요청";
        }
    
    }
    ```

- no-parameter-ajax.html

    ```html
    <html>
      <head>
        <meta charset="utf-8">
      </head>
      <body>
        <script>
          function onReadyStateChange(event) {
            if (ajaxRequest.readyState === XMLHttpRequest.DONE) {
              if (ajaxRequest.status === 200) {
                console.log(ajaxRequest.responseText);
              } else {
                console.error('request failed');
              }
            }
          }
    
          /* ajax 요청 준비 */
          // XMLHttpRequest() 객체 생성
          const ajaxRequest = new XMLHttpRequest();
    
          // onreadystatechange 변수에 정의된 함수 할당 -> ajax 요청이 실행되면 호출되는 함수
          ajaxRequest.onreadystatechange = onReadyStateChange;
          ajaxRequest.open('GET', '/get-with-no-parameter'); // 어떤 경로로 요청할 것인지
    
          /* 실제로 ajax 요청이 발생 */
          ajaxRequest.send();
        </script>
      </body>
    </html>
    ```

- Request: http://localhost:8080/no-parameter-ajax.html 접속

  ![image](https://github.com/user-attachments/assets/de42c5b9-b2b4-4e1c-bb39-c60336eaa91c)

    - no-parameter-ajax.html은 document
    - get-with-no-parameter는 xhr. 즉, ajax 요청

## 실습 2

- Controller.java

    ```java
    @RestController
    public class BookmarkAjaxRestController {
    
        private List<Bookmark> bookmarks = new ArrayList<>();
    
        @RequestMapping(method = RequestMethod.POST, path = "/bookmark")
        public String registerBookmark(@RequestBody Bookmark bookmark) {
            bookmarks.add(bookmark);
            return "registered";
        }
    
        @RequestMapping(method = RequestMethod.GET, path = "/bookmarks")
        public List<Bookmark> getBookmarks() {
            return bookmarks;
        }
    
    }
    ```

- bookmark.java

    ```java
    public class Bookmark {
        public String name;
        public String url;
    }
    ```

- bookmark-ajax.html

    ```html
    <html>
      <head>
        <meta charset="utf-8">
      </head>
      <body>
      <form onsubmit="return addBookmarkRequest();"> <!-- submit 버튼을 누르게 되면 "return addBookmarkRequest();" 가 실행되게 된다 -->
        <label>즐겨찾기 이름 : </label><input type="text" name="name"><br>
        <label>즐겨찾기 URL : </label><input type="text" name="url"><br>
        <input type="submit"><br>
      </form>
      <button onclick="getBookmarkListRequest();">즐겨찾기 목록 가져오기</button> <!-- 클릭하면 getBookmarkListRequest(); 실행 -->
      <ol id="bookmark-list">
        <!-- 여기에 즐겨찾기 목록이 나옵니다. -->
      </ol>
    
      <script>
        function addBookmarkRequest() {
          const name = document.querySelector('input[name=name]').value; // 입력 된 값(name)을 가져와서 name 변수에 할당
          const url = document.querySelector('input[name=url]').value; // 입력 된 값(url)을 가져와서 url 변수에 할당
          const requestObject = {name: name, url: url}; // javascript 객체를 생성해서 넣음
          const requestJson = JSON.stringify(requestObject); // json으로 변경
    
          function onReadyStateChange(event) {
            const currentAjaxRequest = event.currentTarget;
    
            if (currentAjaxRequest.readyState === XMLHttpRequest.DONE) {
              if (currentAjaxRequest.status === 200) {
                alert("즐겨찾기가 등록되었습니다.");
              } else {
                console.error('request failed');
              }
            }
          }
    
          const ajaxRequest = new XMLHttpRequest();
    
          ajaxRequest.onreadystatechange = onReadyStateChange;
          ajaxRequest.open('POST', '/bookmark');
          ajaxRequest.setRequestHeader('Content-Type', 'application/json'); // content-type 헤더 세팅
          ajaxRequest.send(requestJson); // body 와 함께 요청
    
          return false;
          /*
          form 테그의 동작에서는, return true인 경우 form을 제출한 후 페이지를 이동하는 것이 기본 동작으로 정의되어 있음.
          return false로 해주면 form 자체를 제출하지는 않고 ajax로 서버에 요청만 하도록 동작하게 할 수 있다. 때문에 화면도 갱신되지 않는다.
           */
    
        }
    
        function getBookmarkListRequest() {
          function onReadyStateChange(event) {
            const currentAjaxRequest = event.currentTarget;
    
            if (currentAjaxRequest.readyState === XMLHttpRequest.DONE) {
              if (currentAjaxRequest.status === 200) {
                const bookmarkListDom = document.querySelector('#bookmark-list'); // html 에서 bookmark-list라는 아이디를 가진 html 요소를 찾음
                bookmarkListDom.innerHTML = '';
    
                const bookmarks = JSON.parse(currentAjaxRequest.responseText); // 응답을 객체로 직렬화
                bookmarks.forEach(bookmark => {
                  const liNode = document.createElement('li'); // li 테그 생성
                  const textNode = document.createTextNode(bookmark.name + ' - ' + bookmark.url); // text 생성
                  liNode.appendChild(textNode); // li 테그 안쪽에 문자열을 넣음
                  bookmarkListDom.appendChild(liNode); // bookmarkListDom 테그 안쪽에 li 테그를 넣음
                });
              } else {
                console.error('request failed');
              }
            }
          }
    
          const ajaxRequest = new XMLHttpRequest();
    
          ajaxRequest.onreadystatechange = onReadyStateChange;
          ajaxRequest.open('GET', '/bookmarks');
          ajaxRequest.send();
        }
      </script>
      
      <!-- fetch 내장 함수를 사용하여 ajax 요청을 좀 더 현대적으로 보내는 코드 --> 
      
      <script>
        function addBookmarkRequest() {
          const name = document.querySelector('input[name=name]').value;
          const url = document.querySelector('input[name=url]').value;
          const requestObject = { name: name, url: url };
    
          // fetch 내장 함수를 사용하여 ajax 요청을 조금 더 현대적으로 보내는 코드
          fetch('/bookmark', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestObject)
          })
          .then(response => {
            if (response.status === 200) {
              alert("즐겨찾기가 등록되었습니다.");
            } else {
              console.error('request failed');
            }
          })
          .catch(error => {
            console.error('request failed', error);
          });
    
          return false;
        }
    
        function getBookmarkListRequest() {
          fetch('/bookmarks')
          .then(response => {
            if (response.status === 200) {
              return response.json();
            } else {
              console.error('request failed');
              throw new Error('request failed');
            }
          })
          .then(bookmarks => {
            const bookmarkListDom = document.querySelector('#bookmark-list');
            bookmarkListDom.innerHTML = '';
    
            bookmarks.forEach(bookmark => {
              const liNode = document.createElement('li');
              const textNode = document.createTextNode(bookmark.name + ' - ' + bookmark.url);
              liNode.appendChild(textNode);
              bookmarkListDom.appendChild(liNode);
            });
          })
          .catch(error => {
            console.error('request failed', error);
          });
        }
      </script>
      
      </body>
    </html>
    ```
  
# 3. CSS

## 1) css 적용 방법 2가지

1. link 테그 활용 (일반적으로 권장됨)

    ```html
    <link rel="stylesheet" href="style.css">
    ```

2. style 속성 활용 (인라인 지정 - 선택자 사용이 불가능하여 코드 중복이 너무 많아짐)

    ```html
    <span class="custom-style" style="font-size: 30px;">안녕</span>하세요.
    ```

3. style 테그 활용

    ```html
    <style>
    	.custom-style {
    		font-size: 30px;
    	}
    </style>
    ```


## 2) 선택자

어떤 html 요소에 css가 적용되어야 하는지를 선택하는 문법을 말한다.

속성은 중복하여 적용되지만, 중복되는 속성은 우선순위에 따라 적용된다. (구제적인 것이 더 우선된다!)

`!important` > `inline style` > `id 선택자` > `클래스 선택자` > `테그 선택자` > `가상 클래스 선택자`

### 테그 선택자

```html
p {
    color: blue;
}

<p> hello </p>
```

### 클래스 선택자

```
.highlight {
    background-color: yellow;
}

<p class="highlight">This is a highlighted paragraph.</p>
```

- 클래스는 한 테그가 여러 클래스를 가질 수도 있음.

###  아이디 선택

```html
#header {
    font-size: 24px;
}

<h1 id="header">CSS id 선택자 예제</h1>
```

- id는 html 문서 내에 1개만 존재해야함. (문법적으로 강제되는건 아님)

### 자식 선택자

```html
ul > li {
    list-style-type: square;
}

<ul>
    <!-- 적용 됨 -->
    <li>List 1</li>
    <li>
        <!-- 적용 안됨-->		
        <li>List 1-1</li>
    </li>
</ul>

<!-- 적용 안됨-->
<li>List 2</li>
```

- `>` 이 표시는 ul 바로 아래 li 에만 적용한다는 뜻

### 가상 클래스 선택자

```html
a:hover {
    color: red;
}

<a href="#">Hover over me</a>
```

### 속성 선택자

```html
input[type="text"] {
    border: 1px solid gray;
}

<input type="text" placeholder="Text input">
```