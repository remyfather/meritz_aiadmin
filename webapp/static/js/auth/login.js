/**
 * 로그인 페이지 JavaScript
 * 
 * 사용자 로그인 기능을 담당하는 클라이언트 사이드 스크립트입니다.
 * 
 * 주요 기능:
 * - 사용자 입력 검증
 * - 로그인 API 호출 (/api/v2/auth/login)
 * - JWT 토큰 및 사용자 정보 localStorage 저장
 * - 에러 처리 및 사용자 피드백
 * - 로딩 상태 관리
 * 
 * @author Yongho Kim
 * @version 2.0
 * @since 2025
 */

// DOM 로드 완료 시 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', function () {
    // 비밀번호 입력 필드에서 Enter 키 이벤트 처리
    const pwInput = document.getElementById('password');
    pwInput.addEventListener('keypress', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            login();
        }
    })
})

/**
 * 사용자 로그인 함수
 * 
 * 사용자명과 비밀번호를 받아 서버에 인증 요청을 보내고,
 * 성공 시 JWT 토큰과 사용자 정보를 localStorage에 저장합니다.
 * 
 * API 엔드포인트: POST /api/v2/auth/login
 * 요청 데이터: { username, password }
 * 응답 데이터: { success, accessToken, refreshToken, user, message }
 */
async function login() {
    // 사용자 입력 값 가져오기
    const userid = document.getElementById('userid').value;
    const password = document.getElementById('password').value;
    
    // 입력 값 검증
    if (userid === "" || password === "") {
        Swal.fire({
            title: '로그인 실패',
            html: '아이디 혹은 비밀번호를 입력하세요.',
            icon: 'error',
            confirmButtonText: '확인',
            allowOutsideClick: false,
            allowEscapeKey: false
        });
        return;
    }
    
    // 로그인 요청 데이터 구성
    const loginData = {
        username: userid,
        password: password,
    }

    // 로딩 스피너 표시
    showLoading();
    
    try {
        // 로그인 API 호출
        const response = await fetch('/api/v2/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(loginData)
        });
        
        console.log('Raw response:', response);
        
        // HTTP 응답 상태 확인
        if (!response.ok) {
            // HTTP 에러 응답 처리 (401, 500 등)
            const errorData = await response.json();
            console.log('Error response data:', errorData);
            
            let errorMessage = '로그인에 실패했습니다';
            if (errorData && errorData.error) {
                errorMessage = errorData.error;
            }
            
            // 401 에러의 경우 브라우저 리다이렉트 방지
            if (response.status === 401) {
                console.log('401 Unauthorized - preventing browser redirect');
            }
            
            throw new Error(errorMessage);
        }
        
        // 성공 응답 파싱
        const data = await response.json();
        console.log('Success response data:', data);
        
        // 응답 데이터 유효성 검사
        if (!data) {
            throw new Error('서버에서 응답을 받지 못했습니다');
        }
        
        // 성공 여부 확인
        if (data.success === false) {
            const errorMessage = data.error || '로그인에 실패했습니다';
            console.log('Login failed:', errorMessage);
            throw new Error(errorMessage);
        }
        
        // 필수 데이터 확인 (accessToken, user 정보)
        if (!data.accessToken) {
            throw new Error('인증 토큰을 받지 못했습니다');
        }
        
        if (!data.user) {
            throw new Error('사용자 정보를 받지 못했습니다');
        }
        
        // JWT 토큰을 localStorage에 저장
        localStorage.setItem('accessToken', data.accessToken);
        if (data.refreshToken) {
            localStorage.setItem('refreshToken', data.refreshToken);
        }
        
        // 사용자 정보를 localStorage에 저장
        localStorage.setItem('user', JSON.stringify(data.user));
        
        console.log('Login successful for user:', data.user.username);
        
        // 성공 메시지 표시 후 홈페이지로 이동
        Swal.fire({
            title: '로그인 성공',
            html: data.message || '로그인에 성공했습니다',
            icon: 'success',
            timer: 1500,
            showConfirmButton: false,
            allowOutsideClick: false,
            allowEscapeKey: false
        }).then(() => {
            window.location.href = '/';
        });
        
    } catch (error) {
        console.error('Login error:', error);
        
        // 에러 메시지를 SweetAlert로 표시
        Swal.fire({
            title: '로그인 실패',
            html: error.message || '아이디 혹은 비밀번호를 확인하세요.',
            icon: 'error',
            confirmButtonText: '확인',
            allowOutsideClick: false,
            allowEscapeKey: false
        });
    } finally {
        // 로딩 스피너 숨기기
        hideLoading();
    }
}
