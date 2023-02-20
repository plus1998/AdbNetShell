# Overview

You can execute adb commands through the network request.

Use [adblib](https://github.com/tananaev/adblib) and [nanohttpd](https://github.com/NanoHttpd/nanohttpd), run on local.

# Usage

1. Connect adb by USB, restarting in TCP mode.
```shell
adb tcpip 5555
```

2. Execute commands examples;

```shell
# curl
curl --location --request POST 'http://localhost:8999' \
--header 'Content-Type: text/plain' \
--data-raw 'input tap 500 500'
```

```shell
# Auto.js
http.postJson('http://localhost:8999', 'input tap 500 500')
```