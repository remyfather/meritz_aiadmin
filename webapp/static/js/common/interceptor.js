// interceptor.js

(function () {
    // 원본 fetch 함수를 백업
    const originalFetch = window.fetch;

    // 요청 인터셉터 배열
    const requestInterceptors = [];
    // 응답 인터셉터 배열
    const responseInterceptors = [];
    // 오류 인터셉터 배열
    const errorInterceptors = [];

    // fetch 함수를 래핑하여 인터셉터 로직 적용
    window.fetch = function (...args) {
        let [url, options] = args;
        options = options || {};

        // 요청 인터셉터 적용
        // 모든 요청 인터셉터를 순차적으로 적용하여 요청(url, options)을 수정
        let requestPromise = Promise.resolve([url, options]);
        requestInterceptors.forEach(interceptor => {
            requestPromise = requestPromise.then(([currentUrl, currentOptions]) => {
                return interceptor(currentUrl, currentOptions);
            });
        });

        // fetch 호출 및 응답/오류 인터셉터 적용
        return requestPromise
            .then(([finalUrl, finalOptions]) => {
                return originalFetch(finalUrl, finalOptions);
            })
            .then(response => {
                // 응답 인터셉터 적용
                let responsePromise = Promise.resolve(response);
                responseInterceptors.forEach(interceptor => {
                    responsePromise = responsePromise.then(interceptor);
                });
                return responsePromise;
            })
            .catch(error => {
                // 오류 인터셉터 적용
                let errorPromise = Promise.reject(error);
                errorInterceptors.forEach(interceptor => {
                    // 오류 인터셉터는 Promise.reject 또는 Promise.resolve를 반환할 수 있음
                    errorPromise = errorPromise.catch(interceptor);
                });
                return errorPromise;
            });
    };

    // 인터셉터 추가를 위한 API 제공
    window.fetch.interceptors = {
        request: {
            use: (onFulfilled, onRejected) => {
                requestInterceptors.push(onFulfilled);
            }
        },
        response: {
            use: (onFulfilled, onRejected) => {
                responseInterceptors.push(onFulfilled);
                errorInterceptors.push(onRejected);
            }
        }
    };

    fetch.interceptors.request.use((url, options) => {
        options.headers = {
            ...options.headers,
            'Content-Type': 'application/json',
        };
        return [url, options];
    });

    // 응답 인터셉터: 응답 상태 코드에 따라 처리
    fetch.interceptors.response.use(async response => {
        // console.log('Response Interceptor:', response);
        switch (response.status) {
            case 400: {
                return Promise.reject(response);
            }
            case 401: {
                fetch('/api/auth/logout', {
                    method: 'POST'
                }).then(() => window.location.href = '/login');
                return Promise.reject(response);
            }
            case 500: {
                return Promise.reject(response);
            }
        }
        return response;
    }, error => {
        // console.error('Error Interceptor:', error);
        return Promise.reject(error); // 오류 반환
    });
})();