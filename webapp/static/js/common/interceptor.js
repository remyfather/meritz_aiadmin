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
        
        // localStorage에서 accessToken 가져와서 Authorization 헤더에 추가
        const accessToken = localStorage.getItem('accessToken');
        if (accessToken) {
            options.headers['Authorization'] = `Bearer ${accessToken}`;
        }
        
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
                // 삭제된 사용자인지 확인
                const userDeleted = response.headers.get('X-User-Deleted');
                if (userDeleted === 'true') {
                    console.log('User account has been deleted, logging out...');
                    localStorage.removeItem('accessToken');
                    localStorage.removeItem('refreshToken');
                    localStorage.removeItem('user');
                    window.location.href = '/login';
                    return Promise.reject(response);
                }
                
                // 토큰 갱신 시도
                const refreshToken = localStorage.getItem('refreshToken');
                if (refreshToken) {
                    try {
                        const refreshResponse = await fetch('/api/v2/auth/refresh', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                            },
                            body: JSON.stringify({ refreshToken: refreshToken })
                        });
                        
                        if (refreshResponse.ok) {
                            const refreshData = await refreshResponse.json();
                            localStorage.setItem('accessToken', refreshData.accessToken);
                            localStorage.setItem('refreshToken', refreshData.refreshToken);
                            
                            // 원래 요청 재시도
                            const originalRequest = response.config;
                            if (originalRequest) {
                                originalRequest.headers['Authorization'] = `Bearer ${refreshData.accessToken}`;
                                return fetch(originalRequest.url, originalRequest);
                            }
                        }
                    } catch (error) {
                        console.error('Token refresh failed:', error);
                    }
                }
                
                // 토큰 갱신 실패 시 로그아웃
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('user');
                window.location.href = '/login';
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