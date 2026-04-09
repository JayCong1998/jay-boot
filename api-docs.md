---
title: Jay Boot API v1.0
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
highlight_theme: darkula
headingLevel: 2

---

<!-- Generator: Widdershins v4.0.1 -->

<h1 id="jay-boot-api">Jay Boot API v1.0</h1>

> Scroll down for code samples, example requests and responses. Select a language for code samples from the tabs above or the mobile navigation menu.

Jay Boot 项目接口文档

Base URLs:

* <a href="http://localhost:8080">http://localhost:8080</a>

# Authentication

* API Key (satoken)
    - Parameter Name: **satoken**, in: header. 

<h1 id="jay-boot-api--">认证管理</h1>

用户注册、登录、会话与密码管理

## refreshToken

<a id="opIdrefreshToken"></a>

> Code samples

```shell
# You can also use wget
curl -X POST http://localhost:8080/api/auth/token/refresh \
  -H 'Accept: */*'

```

```http
POST http://localhost:8080/api/auth/token/refresh HTTP/1.1
Host: localhost:8080
Accept: */*

```

```javascript

const headers = {
  'Accept':'*/*'
};

fetch('http://localhost:8080/api/auth/token/refresh',
{
  method: 'POST',

  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Accept' => '*/*'
}

result = RestClient.post 'http://localhost:8080/api/auth/token/refresh',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Accept': '*/*'
}

r = requests.post('http://localhost:8080/api/auth/token/refresh', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Accept' => '*/*',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('POST','http://localhost:8080/api/auth/token/refresh', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/auth/token/refresh");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("POST");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Accept": []string{"*/*"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("POST", "http://localhost:8080/api/auth/token/refresh", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`POST /api/auth/token/refresh`

*刷新令牌*

> Example responses

> 200 Response

<h3 id="refreshtoken-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseAuthTokenResponse](#schemaapiresponseauthtokenresponse)|

<aside class="success">
This operation does not require authentication
</aside>

## register

<a id="opIdregister"></a>

> Code samples

```shell
# You can also use wget
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -H 'Accept: */*'

```

```http
POST http://localhost:8080/api/auth/register HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: */*

```

```javascript
const inputBody = '{
  "email": "user@example.com",
  "password": "Password123"
}';
const headers = {
  'Content-Type':'application/json',
  'Accept':'*/*'
};

fetch('http://localhost:8080/api/auth/register',
{
  method: 'POST',
  body: inputBody,
  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Content-Type' => 'application/json',
  'Accept' => '*/*'
}

result = RestClient.post 'http://localhost:8080/api/auth/register',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Content-Type': 'application/json',
  'Accept': '*/*'
}

r = requests.post('http://localhost:8080/api/auth/register', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Content-Type' => 'application/json',
    'Accept' => '*/*',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('POST','http://localhost:8080/api/auth/register', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/auth/register");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("POST");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Content-Type": []string{"application/json"},
        "Accept": []string{"*/*"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("POST", "http://localhost:8080/api/auth/register", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`POST /api/auth/register`

*用户注册*

> Body parameter

```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

<h3 id="register-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|[RegisterRequest](#schemaregisterrequest)|true|none|

> Example responses

> 200 Response

<h3 id="register-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseAuthTokenResponse](#schemaapiresponseauthtokenresponse)|

<aside class="success">
This operation does not require authentication
</aside>

## changePassword

<a id="opIdchangePassword"></a>

> Code samples

```shell
# You can also use wget
curl -X POST http://localhost:8080/api/auth/password/change \
  -H 'Content-Type: application/json' \
  -H 'Accept: */*'

```

```http
POST http://localhost:8080/api/auth/password/change HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: */*

```

```javascript
const inputBody = '{
  "oldPassword": "Password123",
  "newPassword": "Password456"
}';
const headers = {
  'Content-Type':'application/json',
  'Accept':'*/*'
};

fetch('http://localhost:8080/api/auth/password/change',
{
  method: 'POST',
  body: inputBody,
  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Content-Type' => 'application/json',
  'Accept' => '*/*'
}

result = RestClient.post 'http://localhost:8080/api/auth/password/change',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Content-Type': 'application/json',
  'Accept': '*/*'
}

r = requests.post('http://localhost:8080/api/auth/password/change', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Content-Type' => 'application/json',
    'Accept' => '*/*',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('POST','http://localhost:8080/api/auth/password/change', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/auth/password/change");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("POST");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Content-Type": []string{"application/json"},
        "Accept": []string{"*/*"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("POST", "http://localhost:8080/api/auth/password/change", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`POST /api/auth/password/change`

*修改密码*

> Body parameter

```json
{
  "oldPassword": "Password123",
  "newPassword": "Password456"
}
```

<h3 id="changepassword-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|[ChangePasswordRequest](#schemachangepasswordrequest)|true|none|

> Example responses

> 200 Response

<h3 id="changepassword-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseVoid](#schemaapiresponsevoid)|

<aside class="success">
This operation does not require authentication
</aside>

## logout

<a id="opIdlogout"></a>

> Code samples

```shell
# You can also use wget
curl -X POST http://localhost:8080/api/auth/logout \
  -H 'Accept: */*'

```

```http
POST http://localhost:8080/api/auth/logout HTTP/1.1
Host: localhost:8080
Accept: */*

```

```javascript

const headers = {
  'Accept':'*/*'
};

fetch('http://localhost:8080/api/auth/logout',
{
  method: 'POST',

  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Accept' => '*/*'
}

result = RestClient.post 'http://localhost:8080/api/auth/logout',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Accept': '*/*'
}

r = requests.post('http://localhost:8080/api/auth/logout', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Accept' => '*/*',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('POST','http://localhost:8080/api/auth/logout', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/auth/logout");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("POST");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Accept": []string{"*/*"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("POST", "http://localhost:8080/api/auth/logout", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`POST /api/auth/logout`

*用户登出*

> Example responses

> 200 Response

<h3 id="logout-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseVoid](#schemaapiresponsevoid)|

<aside class="success">
This operation does not require authentication
</aside>

## login

<a id="opIdlogin"></a>

> Code samples

```shell
# You can also use wget
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -H 'Accept: */*'

```

```http
POST http://localhost:8080/api/auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: */*

```

```javascript
const inputBody = '{
  "email": "user@example.com",
  "password": "Password123"
}';
const headers = {
  'Content-Type':'application/json',
  'Accept':'*/*'
};

fetch('http://localhost:8080/api/auth/login',
{
  method: 'POST',
  body: inputBody,
  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Content-Type' => 'application/json',
  'Accept' => '*/*'
}

result = RestClient.post 'http://localhost:8080/api/auth/login',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Content-Type': 'application/json',
  'Accept': '*/*'
}

r = requests.post('http://localhost:8080/api/auth/login', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Content-Type' => 'application/json',
    'Accept' => '*/*',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('POST','http://localhost:8080/api/auth/login', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/auth/login");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("POST");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Content-Type": []string{"application/json"},
        "Accept": []string{"*/*"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("POST", "http://localhost:8080/api/auth/login", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`POST /api/auth/login`

*用户登录*

> Body parameter

```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

<h3 id="login-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|[LoginRequest](#schemaloginrequest)|true|none|

> Example responses

> 200 Response

<h3 id="login-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseAuthTokenResponse](#schemaapiresponseauthtokenresponse)|

<aside class="success">
This operation does not require authentication
</aside>

## session

<a id="opIdsession"></a>

> Code samples

```shell
# You can also use wget
curl -X GET http://localhost:8080/api/auth/session \
  -H 'Accept: */*'

```

```http
GET http://localhost:8080/api/auth/session HTTP/1.1
Host: localhost:8080
Accept: */*

```

```javascript

const headers = {
  'Accept':'*/*'
};

fetch('http://localhost:8080/api/auth/session',
{
  method: 'GET',

  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Accept' => '*/*'
}

result = RestClient.get 'http://localhost:8080/api/auth/session',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Accept': '*/*'
}

r = requests.get('http://localhost:8080/api/auth/session', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Accept' => '*/*',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('GET','http://localhost:8080/api/auth/session', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/auth/session");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("GET");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Accept": []string{"*/*"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("GET", "http://localhost:8080/api/auth/session", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`GET /api/auth/session`

*获取当前会话*

> Example responses

> 200 Response

<h3 id="session-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseAuthSessionResponse](#schemaapiresponseauthsessionresponse)|

<aside class="success">
This operation does not require authentication
</aside>

<h1 id="jay-boot-api-rbac-">RBAC 管理</h1>

角色、权限与用户角色分配

## assignUserRoles

<a id="opIdassignUserRoles"></a>

> Code samples

```shell
# You can also use wget
curl -X POST http://localhost:8080/api/rbac/users/{id}/roles \
  -H 'Content-Type: application/json' \
  -H 'Accept: */*' \
  -H 'satoken: API_KEY'

```

```http
POST http://localhost:8080/api/rbac/users/{id}/roles HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: */*

```

```javascript
const inputBody = '{
  "roleIds": [
    0
  ]
}';
const headers = {
  'Content-Type':'application/json',
  'Accept':'*/*',
  'satoken':'API_KEY'
};

fetch('http://localhost:8080/api/rbac/users/{id}/roles',
{
  method: 'POST',
  body: inputBody,
  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Content-Type' => 'application/json',
  'Accept' => '*/*',
  'satoken' => 'API_KEY'
}

result = RestClient.post 'http://localhost:8080/api/rbac/users/{id}/roles',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Content-Type': 'application/json',
  'Accept': '*/*',
  'satoken': 'API_KEY'
}

r = requests.post('http://localhost:8080/api/rbac/users/{id}/roles', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Content-Type' => 'application/json',
    'Accept' => '*/*',
    'satoken' => 'API_KEY',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('POST','http://localhost:8080/api/rbac/users/{id}/roles', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/rbac/users/{id}/roles");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("POST");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Content-Type": []string{"application/json"},
        "Accept": []string{"*/*"},
        "satoken": []string{"API_KEY"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("POST", "http://localhost:8080/api/rbac/users/{id}/roles", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`POST /api/rbac/users/{id}/roles`

*用户分配角色*

> Body parameter

```json
{
  "roleIds": [
    0
  ]
}
```

<h3 id="assignuserroles-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|id|path|integer(int64)|true|none|
|body|body|[UserAssignRolesRequest](#schemauserassignrolesrequest)|true|none|

> Example responses

> 200 Response

<h3 id="assignuserroles-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseVoid](#schemaapiresponsevoid)|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
satoken
</aside>

## roles

<a id="opIdroles"></a>

> Code samples

```shell
# You can also use wget
curl -X GET http://localhost:8080/api/rbac/roles \
  -H 'Accept: */*' \
  -H 'satoken: API_KEY'

```

```http
GET http://localhost:8080/api/rbac/roles HTTP/1.1
Host: localhost:8080
Accept: */*

```

```javascript

const headers = {
  'Accept':'*/*',
  'satoken':'API_KEY'
};

fetch('http://localhost:8080/api/rbac/roles',
{
  method: 'GET',

  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Accept' => '*/*',
  'satoken' => 'API_KEY'
}

result = RestClient.get 'http://localhost:8080/api/rbac/roles',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Accept': '*/*',
  'satoken': 'API_KEY'
}

r = requests.get('http://localhost:8080/api/rbac/roles', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Accept' => '*/*',
    'satoken' => 'API_KEY',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('GET','http://localhost:8080/api/rbac/roles', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/rbac/roles");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("GET");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Accept": []string{"*/*"},
        "satoken": []string{"API_KEY"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("GET", "http://localhost:8080/api/rbac/roles", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`GET /api/rbac/roles`

*查询角色列表*

> Example responses

> 200 Response

<h3 id="roles-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseListRoleResponse](#schemaapiresponselistroleresponse)|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
satoken
</aside>

## createRole

<a id="opIdcreateRole"></a>

> Code samples

```shell
# You can also use wget
curl -X POST http://localhost:8080/api/rbac/roles \
  -H 'Content-Type: application/json' \
  -H 'Accept: */*' \
  -H 'satoken: API_KEY'

```

```http
POST http://localhost:8080/api/rbac/roles HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: */*

```

```javascript
const inputBody = '{
  "name": "MANAGER"
}';
const headers = {
  'Content-Type':'application/json',
  'Accept':'*/*',
  'satoken':'API_KEY'
};

fetch('http://localhost:8080/api/rbac/roles',
{
  method: 'POST',
  body: inputBody,
  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Content-Type' => 'application/json',
  'Accept' => '*/*',
  'satoken' => 'API_KEY'
}

result = RestClient.post 'http://localhost:8080/api/rbac/roles',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Content-Type': 'application/json',
  'Accept': '*/*',
  'satoken': 'API_KEY'
}

r = requests.post('http://localhost:8080/api/rbac/roles', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Content-Type' => 'application/json',
    'Accept' => '*/*',
    'satoken' => 'API_KEY',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('POST','http://localhost:8080/api/rbac/roles', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/rbac/roles");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("POST");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Content-Type": []string{"application/json"},
        "Accept": []string{"*/*"},
        "satoken": []string{"API_KEY"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("POST", "http://localhost:8080/api/rbac/roles", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`POST /api/rbac/roles`

*创建角色*

> Body parameter

```json
{
  "name": "MANAGER"
}
```

<h3 id="createrole-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|[RoleCreateRequest](#schemarolecreaterequest)|true|none|

> Example responses

> 200 Response

<h3 id="createrole-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseRoleResponse](#schemaapiresponseroleresponse)|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
satoken
</aside>

## assignPermissions

<a id="opIdassignPermissions"></a>

> Code samples

```shell
# You can also use wget
curl -X POST http://localhost:8080/api/rbac/roles/{id}/permissions \
  -H 'Content-Type: application/json' \
  -H 'Accept: */*' \
  -H 'satoken: API_KEY'

```

```http
POST http://localhost:8080/api/rbac/roles/{id}/permissions HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: */*

```

```javascript
const inputBody = '{
  "permissionCodes": [
    "string"
  ]
}';
const headers = {
  'Content-Type':'application/json',
  'Accept':'*/*',
  'satoken':'API_KEY'
};

fetch('http://localhost:8080/api/rbac/roles/{id}/permissions',
{
  method: 'POST',
  body: inputBody,
  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Content-Type' => 'application/json',
  'Accept' => '*/*',
  'satoken' => 'API_KEY'
}

result = RestClient.post 'http://localhost:8080/api/rbac/roles/{id}/permissions',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Content-Type': 'application/json',
  'Accept': '*/*',
  'satoken': 'API_KEY'
}

r = requests.post('http://localhost:8080/api/rbac/roles/{id}/permissions', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Content-Type' => 'application/json',
    'Accept' => '*/*',
    'satoken' => 'API_KEY',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('POST','http://localhost:8080/api/rbac/roles/{id}/permissions', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/rbac/roles/{id}/permissions");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("POST");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Content-Type": []string{"application/json"},
        "Accept": []string{"*/*"},
        "satoken": []string{"API_KEY"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("POST", "http://localhost:8080/api/rbac/roles/{id}/permissions", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`POST /api/rbac/roles/{id}/permissions`

*角色分配权限*

> Body parameter

```json
{
  "permissionCodes": [
    "string"
  ]
}
```

<h3 id="assignpermissions-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|id|path|integer(int64)|true|none|
|body|body|[RoleAssignPermissionsRequest](#schemaroleassignpermissionsrequest)|true|none|

> Example responses

> 200 Response

<h3 id="assignpermissions-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseVoid](#schemaapiresponsevoid)|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
satoken
</aside>

## checkPermission

<a id="opIdcheckPermission"></a>

> Code samples

```shell
# You can also use wget
curl -X GET http://localhost:8080/api/rbac/permissions/check?code=string \
  -H 'Accept: */*' \
  -H 'satoken: API_KEY'

```

```http
GET http://localhost:8080/api/rbac/permissions/check?code=string HTTP/1.1
Host: localhost:8080
Accept: */*

```

```javascript

const headers = {
  'Accept':'*/*',
  'satoken':'API_KEY'
};

fetch('http://localhost:8080/api/rbac/permissions/check?code=string',
{
  method: 'GET',

  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Accept' => '*/*',
  'satoken' => 'API_KEY'
}

result = RestClient.get 'http://localhost:8080/api/rbac/permissions/check',
  params: {
  'code' => 'string'
}, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Accept': '*/*',
  'satoken': 'API_KEY'
}

r = requests.get('http://localhost:8080/api/rbac/permissions/check', params={
  'code': 'string'
}, headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Accept' => '*/*',
    'satoken' => 'API_KEY',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('GET','http://localhost:8080/api/rbac/permissions/check', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/rbac/permissions/check?code=string");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("GET");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Accept": []string{"*/*"},
        "satoken": []string{"API_KEY"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("GET", "http://localhost:8080/api/rbac/permissions/check", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`GET /api/rbac/permissions/check`

*检查当前用户权限*

<h3 id="checkpermission-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|code|query|string|true|none|

> Example responses

> 200 Response

<h3 id="checkpermission-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponsePermissionCheckResponse](#schemaapiresponsepermissioncheckresponse)|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
satoken
</aside>

<h1 id="jay-boot-api--">系统状态</h1>

系统健康检查接口

## ping

<a id="opIdping"></a>

> Code samples

```shell
# You can also use wget
curl -X GET http://localhost:8080/api/system/ping \
  -H 'Accept: */*'

```

```http
GET http://localhost:8080/api/system/ping HTTP/1.1
Host: localhost:8080
Accept: */*

```

```javascript

const headers = {
  'Accept':'*/*'
};

fetch('http://localhost:8080/api/system/ping',
{
  method: 'GET',

  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Accept' => '*/*'
}

result = RestClient.get 'http://localhost:8080/api/system/ping',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Accept': '*/*'
}

r = requests.get('http://localhost:8080/api/system/ping', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Accept' => '*/*',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('GET','http://localhost:8080/api/system/ping', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/system/ping");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("GET");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Accept": []string{"*/*"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("GET", "http://localhost:8080/api/system/ping", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`GET /api/system/ping`

*系统存活检查*

> Example responses

> 200 Response

<h3 id="ping-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|Inline|

<h3 id="ping-responseschema">Response Schema</h3>

Status Code **200**

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|» **additionalProperties**|object|false|none|none|

<aside class="success">
This operation does not require authentication
</aside>

<h1 id="jay-boot-api-billing-">Billing 管理</h1>

套餐与订阅管理

## createSubscription

<a id="opIdcreateSubscription"></a>

> Code samples

```shell
# You can also use wget
curl -X POST http://localhost:8080/api/billing/subscriptions \
  -H 'Content-Type: application/json' \
  -H 'Accept: */*' \
  -H 'satoken: API_KEY'

```

```http
POST http://localhost:8080/api/billing/subscriptions HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: */*

```

```javascript
const inputBody = '{
  "planId": 1001,
  "initialStatus": "TRIALING",
  "trialEndAt": "2019-08-24T14:15:22Z",
  "currentPeriodEnd": "2019-08-24T14:15:22Z"
}';
const headers = {
  'Content-Type':'application/json',
  'Accept':'*/*',
  'satoken':'API_KEY'
};

fetch('http://localhost:8080/api/billing/subscriptions',
{
  method: 'POST',
  body: inputBody,
  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Content-Type' => 'application/json',
  'Accept' => '*/*',
  'satoken' => 'API_KEY'
}

result = RestClient.post 'http://localhost:8080/api/billing/subscriptions',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Content-Type': 'application/json',
  'Accept': '*/*',
  'satoken': 'API_KEY'
}

r = requests.post('http://localhost:8080/api/billing/subscriptions', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Content-Type' => 'application/json',
    'Accept' => '*/*',
    'satoken' => 'API_KEY',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('POST','http://localhost:8080/api/billing/subscriptions', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/billing/subscriptions");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("POST");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Content-Type": []string{"application/json"},
        "Accept": []string{"*/*"},
        "satoken": []string{"API_KEY"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("POST", "http://localhost:8080/api/billing/subscriptions", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`POST /api/billing/subscriptions`

*创建当前租户订阅*

> Body parameter

```json
{
  "planId": 1001,
  "initialStatus": "TRIALING",
  "trialEndAt": "2019-08-24T14:15:22Z",
  "currentPeriodEnd": "2019-08-24T14:15:22Z"
}
```

<h3 id="createsubscription-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|[SubscriptionCreateRequest](#schemasubscriptioncreaterequest)|true|none|

> Example responses

> 200 Response

<h3 id="createsubscription-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseSubscriptionView](#schemaapiresponsesubscriptionview)|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
satoken
</aside>

## updateSubscription

<a id="opIdupdateSubscription"></a>

> Code samples

```shell
# You can also use wget
curl -X POST http://localhost:8080/api/billing/subscriptions/{id}/update \
  -H 'Content-Type: application/json' \
  -H 'Accept: */*' \
  -H 'satoken: API_KEY'

```

```http
POST http://localhost:8080/api/billing/subscriptions/{id}/update HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: */*

```

```javascript
const inputBody = '{
  "targetStatus": "ACTIVE",
  "currentPeriodEnd": "2019-08-24T14:15:22Z",
  "cancelTime": "2019-08-24T14:15:22Z"
}';
const headers = {
  'Content-Type':'application/json',
  'Accept':'*/*',
  'satoken':'API_KEY'
};

fetch('http://localhost:8080/api/billing/subscriptions/{id}/update',
{
  method: 'POST',
  body: inputBody,
  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Content-Type' => 'application/json',
  'Accept' => '*/*',
  'satoken' => 'API_KEY'
}

result = RestClient.post 'http://localhost:8080/api/billing/subscriptions/{id}/update',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Content-Type': 'application/json',
  'Accept': '*/*',
  'satoken': 'API_KEY'
}

r = requests.post('http://localhost:8080/api/billing/subscriptions/{id}/update', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Content-Type' => 'application/json',
    'Accept' => '*/*',
    'satoken' => 'API_KEY',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('POST','http://localhost:8080/api/billing/subscriptions/{id}/update', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/billing/subscriptions/{id}/update");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("POST");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Content-Type": []string{"application/json"},
        "Accept": []string{"*/*"},
        "satoken": []string{"API_KEY"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("POST", "http://localhost:8080/api/billing/subscriptions/{id}/update", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`POST /api/billing/subscriptions/{id}/update`

*更新订阅状态*

> Body parameter

```json
{
  "targetStatus": "ACTIVE",
  "currentPeriodEnd": "2019-08-24T14:15:22Z",
  "cancelTime": "2019-08-24T14:15:22Z"
}
```

<h3 id="updatesubscription-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|id|path|integer(int64)|true|none|
|body|body|[SubscriptionUpdateRequest](#schemasubscriptionupdaterequest)|true|none|

> Example responses

> 200 Response

<h3 id="updatesubscription-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseSubscriptionView](#schemaapiresponsesubscriptionview)|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
satoken
</aside>

## plans

<a id="opIdplans"></a>

> Code samples

```shell
# You can also use wget
curl -X GET http://localhost:8080/api/billing/plans \
  -H 'Accept: */*' \
  -H 'satoken: API_KEY'

```

```http
GET http://localhost:8080/api/billing/plans HTTP/1.1
Host: localhost:8080
Accept: */*

```

```javascript

const headers = {
  'Accept':'*/*',
  'satoken':'API_KEY'
};

fetch('http://localhost:8080/api/billing/plans',
{
  method: 'GET',

  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Accept' => '*/*',
  'satoken' => 'API_KEY'
}

result = RestClient.get 'http://localhost:8080/api/billing/plans',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Accept': '*/*',
  'satoken': 'API_KEY'
}

r = requests.get('http://localhost:8080/api/billing/plans', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Accept' => '*/*',
    'satoken' => 'API_KEY',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('GET','http://localhost:8080/api/billing/plans', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/billing/plans");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("GET");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Accept": []string{"*/*"},
        "satoken": []string{"API_KEY"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("GET", "http://localhost:8080/api/billing/plans", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`GET /api/billing/plans`

*查询套餐列表*

> Example responses

> 200 Response

<h3 id="plans-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseListPlanView](#schemaapiresponselistplanview)|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
satoken
</aside>

<h1 id="jay-boot-api--">租户管理</h1>

当前租户查询与更新

## updateCurrent

<a id="opIdupdateCurrent"></a>

> Code samples

```shell
# You can also use wget
curl -X POST http://localhost:8080/api/tenants/current/update \
  -H 'Content-Type: application/json' \
  -H 'Accept: */*' \
  -H 'satoken: API_KEY'

```

```http
POST http://localhost:8080/api/tenants/current/update HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: */*

```

```javascript
const inputBody = '{
  "name": "Acme Workspace"
}';
const headers = {
  'Content-Type':'application/json',
  'Accept':'*/*',
  'satoken':'API_KEY'
};

fetch('http://localhost:8080/api/tenants/current/update',
{
  method: 'POST',
  body: inputBody,
  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Content-Type' => 'application/json',
  'Accept' => '*/*',
  'satoken' => 'API_KEY'
}

result = RestClient.post 'http://localhost:8080/api/tenants/current/update',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Content-Type': 'application/json',
  'Accept': '*/*',
  'satoken': 'API_KEY'
}

r = requests.post('http://localhost:8080/api/tenants/current/update', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Content-Type' => 'application/json',
    'Accept' => '*/*',
    'satoken' => 'API_KEY',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('POST','http://localhost:8080/api/tenants/current/update', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/tenants/current/update");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("POST");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Content-Type": []string{"application/json"},
        "Accept": []string{"*/*"},
        "satoken": []string{"API_KEY"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("POST", "http://localhost:8080/api/tenants/current/update", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`POST /api/tenants/current/update`

*更新当前租户*

> Body parameter

```json
{
  "name": "Acme Workspace"
}
```

<h3 id="updatecurrent-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|[TenantUpdateCurrentRequest](#schematenantupdatecurrentrequest)|true|none|

> Example responses

> 200 Response

<h3 id="updatecurrent-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseTenantCurrentResponse](#schemaapiresponsetenantcurrentresponse)|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
satoken
</aside>

## current

<a id="opIdcurrent"></a>

> Code samples

```shell
# You can also use wget
curl -X GET http://localhost:8080/api/tenants/current \
  -H 'Accept: */*' \
  -H 'satoken: API_KEY'

```

```http
GET http://localhost:8080/api/tenants/current HTTP/1.1
Host: localhost:8080
Accept: */*

```

```javascript

const headers = {
  'Accept':'*/*',
  'satoken':'API_KEY'
};

fetch('http://localhost:8080/api/tenants/current',
{
  method: 'GET',

  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Accept' => '*/*',
  'satoken' => 'API_KEY'
}

result = RestClient.get 'http://localhost:8080/api/tenants/current',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Accept': '*/*',
  'satoken': 'API_KEY'
}

r = requests.get('http://localhost:8080/api/tenants/current', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Accept' => '*/*',
    'satoken' => 'API_KEY',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('GET','http://localhost:8080/api/tenants/current', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/api/tenants/current");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("GET");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Accept": []string{"*/*"},
        "satoken": []string{"API_KEY"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("GET", "http://localhost:8080/api/tenants/current", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`GET /api/tenants/current`

*查询当前租户*

> Example responses

> 200 Response

<h3 id="current-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseTenantCurrentResponse](#schemaapiresponsetenantcurrentresponse)|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
satoken
</aside>

# Schemas

<h2 id="tocS_TenantUpdateCurrentRequest">TenantUpdateCurrentRequest</h2>
<!-- backwards compatibility -->
<a id="schematenantupdatecurrentrequest"></a>
<a id="schema_TenantUpdateCurrentRequest"></a>
<a id="tocStenantupdatecurrentrequest"></a>
<a id="tocstenantupdatecurrentrequest"></a>

```json
{
  "name": "Acme Workspace"
}

```

更新当前租户请求

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|name|string|true|none|工作区名称|

<h2 id="tocS_ApiResponseTenantCurrentResponse">ApiResponseTenantCurrentResponse</h2>
<!-- backwards compatibility -->
<a id="schemaapiresponsetenantcurrentresponse"></a>
<a id="schema_ApiResponseTenantCurrentResponse"></a>
<a id="tocSapiresponsetenantcurrentresponse"></a>
<a id="tocsapiresponsetenantcurrentresponse"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "tenantId": 2001,
    "name": "workspace-a1b2",
    "ownerUserId": 1001,
    "planCode": "FREE"
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|code|integer(int32)|false|none|none|
|message|string|false|none|none|
|data|[TenantCurrentResponse](#schematenantcurrentresponse)|false|none|当前租户信息|

<h2 id="tocS_TenantCurrentResponse">TenantCurrentResponse</h2>
<!-- backwards compatibility -->
<a id="schematenantcurrentresponse"></a>
<a id="schema_TenantCurrentResponse"></a>
<a id="tocStenantcurrentresponse"></a>
<a id="tocstenantcurrentresponse"></a>

```json
{
  "tenantId": 2001,
  "name": "workspace-a1b2",
  "ownerUserId": 1001,
  "planCode": "FREE"
}

```

当前租户信息

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|tenantId|integer(int64)|false|none|租户 ID|
|name|string|false|none|工作区名称|
|ownerUserId|integer(int64)|false|none|租户拥有者用户 ID|
|planCode|string|false|none|套餐编码|

<h2 id="tocS_UserAssignRolesRequest">UserAssignRolesRequest</h2>
<!-- backwards compatibility -->
<a id="schemauserassignrolesrequest"></a>
<a id="schema_UserAssignRolesRequest"></a>
<a id="tocSuserassignrolesrequest"></a>
<a id="tocsuserassignrolesrequest"></a>

```json
{
  "roleIds": [
    0
  ]
}

```

用户角色分配请求

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|roleIds|[integer]|true|none|角色 ID 列表|

<h2 id="tocS_ApiResponseVoid">ApiResponseVoid</h2>
<!-- backwards compatibility -->
<a id="schemaapiresponsevoid"></a>
<a id="schema_ApiResponseVoid"></a>
<a id="tocSapiresponsevoid"></a>
<a id="tocsapiresponsevoid"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {}
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|code|integer(int32)|false|none|none|
|message|string|false|none|none|
|data|object|false|none|none|

<h2 id="tocS_RoleCreateRequest">RoleCreateRequest</h2>
<!-- backwards compatibility -->
<a id="schemarolecreaterequest"></a>
<a id="schema_RoleCreateRequest"></a>
<a id="tocSrolecreaterequest"></a>
<a id="tocsrolecreaterequest"></a>

```json
{
  "name": "MANAGER"
}

```

角色创建请求

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|name|string|true|none|角色名称|

<h2 id="tocS_ApiResponseRoleResponse">ApiResponseRoleResponse</h2>
<!-- backwards compatibility -->
<a id="schemaapiresponseroleresponse"></a>
<a id="schema_ApiResponseRoleResponse"></a>
<a id="tocSapiresponseroleresponse"></a>
<a id="tocsapiresponseroleresponse"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "roleId": 3001,
    "name": "MANAGER"
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|code|integer(int32)|false|none|none|
|message|string|false|none|none|
|data|[RoleResponse](#schemaroleresponse)|false|none|角色响应|

<h2 id="tocS_RoleResponse">RoleResponse</h2>
<!-- backwards compatibility -->
<a id="schemaroleresponse"></a>
<a id="schema_RoleResponse"></a>
<a id="tocSroleresponse"></a>
<a id="tocsroleresponse"></a>

```json
{
  "roleId": 3001,
  "name": "MANAGER"
}

```

角色响应

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|roleId|integer(int64)|false|none|角色 ID|
|name|string|false|none|角色名称|

<h2 id="tocS_RoleAssignPermissionsRequest">RoleAssignPermissionsRequest</h2>
<!-- backwards compatibility -->
<a id="schemaroleassignpermissionsrequest"></a>
<a id="schema_RoleAssignPermissionsRequest"></a>
<a id="tocSroleassignpermissionsrequest"></a>
<a id="tocsroleassignpermissionsrequest"></a>

```json
{
  "permissionCodes": [
    "string"
  ]
}

```

角色权限分配请求

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|permissionCodes|[string]|true|none|权限编码列表|

<h2 id="tocS_SubscriptionCreateRequest">SubscriptionCreateRequest</h2>
<!-- backwards compatibility -->
<a id="schemasubscriptioncreaterequest"></a>
<a id="schema_SubscriptionCreateRequest"></a>
<a id="tocSsubscriptioncreaterequest"></a>
<a id="tocssubscriptioncreaterequest"></a>

```json
{
  "planId": 1001,
  "initialStatus": "TRIALING",
  "trialEndAt": "2019-08-24T14:15:22Z",
  "currentPeriodEnd": "2019-08-24T14:15:22Z"
}

```

创建订阅请求

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|planId|integer(int64)|true|none|套餐 ID|
|initialStatus|string|false|none|初始状态，仅支持 TRIALING 或 ACTIVE，默认 TRIALING|
|trialEndAt|string(date-time)|false|none|试用结束时间|
|currentPeriodEnd|string(date-time)|false|none|当前计费周期结束时间|

<h2 id="tocS_ApiResponseSubscriptionView">ApiResponseSubscriptionView</h2>
<!-- backwards compatibility -->
<a id="schemaapiresponsesubscriptionview"></a>
<a id="schema_ApiResponseSubscriptionView"></a>
<a id="tocSapiresponsesubscriptionview"></a>
<a id="tocsapiresponsesubscriptionview"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "subscriptionId": 2001,
    "tenantId": 3001,
    "planId": 1001,
    "status": "TRIALING",
    "trialEndAt": "2019-08-24T14:15:22Z",
    "currentPeriodEnd": "2019-08-24T14:15:22Z",
    "effectiveTime": "2019-08-24T14:15:22Z",
    "cancelTime": "2019-08-24T14:15:22Z"
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|code|integer(int32)|false|none|none|
|message|string|false|none|none|
|data|[SubscriptionView](#schemasubscriptionview)|false|none|订阅信息|

<h2 id="tocS_SubscriptionView">SubscriptionView</h2>
<!-- backwards compatibility -->
<a id="schemasubscriptionview"></a>
<a id="schema_SubscriptionView"></a>
<a id="tocSsubscriptionview"></a>
<a id="tocssubscriptionview"></a>

```json
{
  "subscriptionId": 2001,
  "tenantId": 3001,
  "planId": 1001,
  "status": "TRIALING",
  "trialEndAt": "2019-08-24T14:15:22Z",
  "currentPeriodEnd": "2019-08-24T14:15:22Z",
  "effectiveTime": "2019-08-24T14:15:22Z",
  "cancelTime": "2019-08-24T14:15:22Z"
}

```

订阅信息

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|subscriptionId|integer(int64)|false|none|订阅 ID|
|tenantId|integer(int64)|false|none|租户 ID|
|planId|integer(int64)|false|none|套餐 ID|
|status|string|false|none|订阅状态|
|trialEndAt|string(date-time)|false|none|试用结束时间|
|currentPeriodEnd|string(date-time)|false|none|当前计费周期结束时间|
|effectiveTime|string(date-time)|false|none|生效时间|
|cancelTime|string(date-time)|false|none|取消时间|

<h2 id="tocS_SubscriptionUpdateRequest">SubscriptionUpdateRequest</h2>
<!-- backwards compatibility -->
<a id="schemasubscriptionupdaterequest"></a>
<a id="schema_SubscriptionUpdateRequest"></a>
<a id="tocSsubscriptionupdaterequest"></a>
<a id="tocssubscriptionupdaterequest"></a>

```json
{
  "targetStatus": "ACTIVE",
  "currentPeriodEnd": "2019-08-24T14:15:22Z",
  "cancelTime": "2019-08-24T14:15:22Z"
}

```

更新订阅请求

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|targetStatus|string|true|none|目标状态|
|currentPeriodEnd|string(date-time)|false|none|当前计费周期结束时间|
|cancelTime|string(date-time)|false|none|取消时间（目标状态为 CANCELED 时可传）|

<h2 id="tocS_ApiResponseAuthTokenResponse">ApiResponseAuthTokenResponse</h2>
<!-- backwards compatibility -->
<a id="schemaapiresponseauthtokenresponse"></a>
<a id="schema_ApiResponseAuthTokenResponse"></a>
<a id="tocSapiresponseauthtokenresponse"></a>
<a id="tocsapiresponseauthtokenresponse"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "token": "string",
    "tokenTimeout": 0,
    "user": {
      "id": 1001,
      "tenantId": 2001,
      "email": "user@example.com",
      "status": "ACTIVE"
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|code|integer(int32)|false|none|none|
|message|string|false|none|none|
|data|[AuthTokenResponse](#schemaauthtokenresponse)|false|none|认证令牌响应|

<h2 id="tocS_AuthTokenResponse">AuthTokenResponse</h2>
<!-- backwards compatibility -->
<a id="schemaauthtokenresponse"></a>
<a id="schema_AuthTokenResponse"></a>
<a id="tocSauthtokenresponse"></a>
<a id="tocsauthtokenresponse"></a>

```json
{
  "token": "string",
  "tokenTimeout": 0,
  "user": {
    "id": 1001,
    "tenantId": 2001,
    "email": "user@example.com",
    "status": "ACTIVE"
  }
}

```

认证令牌响应

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|token|string|false|none|访问令牌|
|tokenTimeout|integer(int64)|false|none|令牌有效期（秒）|
|user|[AuthUserView](#schemaauthuserview)|false|none|登录用户信息|

<h2 id="tocS_AuthUserView">AuthUserView</h2>
<!-- backwards compatibility -->
<a id="schemaauthuserview"></a>
<a id="schema_AuthUserView"></a>
<a id="tocSauthuserview"></a>
<a id="tocsauthuserview"></a>

```json
{
  "id": 1001,
  "tenantId": 2001,
  "email": "user@example.com",
  "status": "ACTIVE"
}

```

登录用户信息

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|id|integer(int64)|false|none|用户 ID|
|tenantId|integer(int64)|false|none|租户 ID|
|email|string|false|none|邮箱|
|status|string|false|none|用户状态|

<h2 id="tocS_RegisterRequest">RegisterRequest</h2>
<!-- backwards compatibility -->
<a id="schemaregisterrequest"></a>
<a id="schema_RegisterRequest"></a>
<a id="tocSregisterrequest"></a>
<a id="tocsregisterrequest"></a>

```json
{
  "email": "user@example.com",
  "password": "Password123"
}

```

用户注册请求

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|email|string|true|none|邮箱|
|password|string|true|none|密码|

<h2 id="tocS_ChangePasswordRequest">ChangePasswordRequest</h2>
<!-- backwards compatibility -->
<a id="schemachangepasswordrequest"></a>
<a id="schema_ChangePasswordRequest"></a>
<a id="tocSchangepasswordrequest"></a>
<a id="tocschangepasswordrequest"></a>

```json
{
  "oldPassword": "Password123",
  "newPassword": "Password456"
}

```

修改密码请求

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|oldPassword|string|true|none|旧密码|
|newPassword|string|true|none|新密码|

<h2 id="tocS_LoginRequest">LoginRequest</h2>
<!-- backwards compatibility -->
<a id="schemaloginrequest"></a>
<a id="schema_LoginRequest"></a>
<a id="tocSloginrequest"></a>
<a id="tocsloginrequest"></a>

```json
{
  "email": "user@example.com",
  "password": "Password123"
}

```

用户登录请求

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|email|string|true|none|邮箱|
|password|string|true|none|密码|

<h2 id="tocS_ApiResponseListRoleResponse">ApiResponseListRoleResponse</h2>
<!-- backwards compatibility -->
<a id="schemaapiresponselistroleresponse"></a>
<a id="schema_ApiResponseListRoleResponse"></a>
<a id="tocSapiresponselistroleresponse"></a>
<a id="tocsapiresponselistroleresponse"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": [
    {
      "roleId": 3001,
      "name": "MANAGER"
    }
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|code|integer(int32)|false|none|none|
|message|string|false|none|none|
|data|[[RoleResponse](#schemaroleresponse)]|false|none|[角色响应]|

<h2 id="tocS_ApiResponsePermissionCheckResponse">ApiResponsePermissionCheckResponse</h2>
<!-- backwards compatibility -->
<a id="schemaapiresponsepermissioncheckresponse"></a>
<a id="schema_ApiResponsePermissionCheckResponse"></a>
<a id="tocSapiresponsepermissioncheckresponse"></a>
<a id="tocsapiresponsepermissioncheckresponse"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "code": "rbac.role.read",
    "granted": true
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|code|integer(int32)|false|none|none|
|message|string|false|none|none|
|data|[PermissionCheckResponse](#schemapermissioncheckresponse)|false|none|权限检查响应|

<h2 id="tocS_PermissionCheckResponse">PermissionCheckResponse</h2>
<!-- backwards compatibility -->
<a id="schemapermissioncheckresponse"></a>
<a id="schema_PermissionCheckResponse"></a>
<a id="tocSpermissioncheckresponse"></a>
<a id="tocspermissioncheckresponse"></a>

```json
{
  "code": "rbac.role.read",
  "granted": true
}

```

权限检查响应

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|code|string|false|none|权限编码|
|granted|boolean|false|none|是否已授予|

<h2 id="tocS_ApiResponseListPlanView">ApiResponseListPlanView</h2>
<!-- backwards compatibility -->
<a id="schemaapiresponselistplanview"></a>
<a id="schema_ApiResponseListPlanView"></a>
<a id="tocSapiresponselistplanview"></a>
<a id="tocsapiresponselistplanview"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": [
    {
      "planId": 1001,
      "code": "PRO",
      "name": "专业版",
      "billingCycle": "MONTHLY",
      "quotaJson": "{\"users\":10,\"storageGb\":100}",
      "price": 19900,
      "status": "ACTIVE"
    }
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|code|integer(int32)|false|none|none|
|message|string|false|none|none|
|data|[[PlanView](#schemaplanview)]|false|none|[套餐信息]|

<h2 id="tocS_PlanView">PlanView</h2>
<!-- backwards compatibility -->
<a id="schemaplanview"></a>
<a id="schema_PlanView"></a>
<a id="tocSplanview"></a>
<a id="tocsplanview"></a>

```json
{
  "planId": 1001,
  "code": "PRO",
  "name": "专业版",
  "billingCycle": "MONTHLY",
  "quotaJson": "{\"users\":10,\"storageGb\":100}",
  "price": 19900,
  "status": "ACTIVE"
}

```

套餐信息

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|planId|integer(int64)|false|none|套餐 ID|
|code|string|false|none|套餐编码|
|name|string|false|none|套餐名称|
|billingCycle|string|false|none|计费周期|
|quotaJson|string|false|none|额度配置 JSON|
|price|integer(int64)|false|none|价格（最小货币单位）|
|status|string|false|none|套餐状态|

<h2 id="tocS_ApiResponseAuthSessionResponse">ApiResponseAuthSessionResponse</h2>
<!-- backwards compatibility -->
<a id="schemaapiresponseauthsessionresponse"></a>
<a id="schema_ApiResponseAuthSessionResponse"></a>
<a id="tocSapiresponseauthsessionresponse"></a>
<a id="tocsapiresponseauthsessionresponse"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "loginId": 0,
    "token": "string",
    "tokenTimeout": 0,
    "user": {
      "id": 1001,
      "tenantId": 2001,
      "email": "user@example.com",
      "status": "ACTIVE"
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|code|integer(int32)|false|none|none|
|message|string|false|none|none|
|data|[AuthSessionResponse](#schemaauthsessionresponse)|false|none|当前会话响应|

<h2 id="tocS_AuthSessionResponse">AuthSessionResponse</h2>
<!-- backwards compatibility -->
<a id="schemaauthsessionresponse"></a>
<a id="schema_AuthSessionResponse"></a>
<a id="tocSauthsessionresponse"></a>
<a id="tocsauthsessionresponse"></a>

```json
{
  "loginId": 0,
  "token": "string",
  "tokenTimeout": 0,
  "user": {
    "id": 1001,
    "tenantId": 2001,
    "email": "user@example.com",
    "status": "ACTIVE"
  }
}

```

当前会话响应

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|loginId|integer(int64)|false|none|登录用户 ID|
|token|string|false|none|当前令牌|
|tokenTimeout|integer(int64)|false|none|令牌有效期（秒）|
|user|[AuthUserView](#schemaauthuserview)|false|none|登录用户信息|

