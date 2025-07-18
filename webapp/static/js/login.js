document.addEventListener('DOMContentLoaded', function () {
    const pwInput = document.getElementById('password');
    pwInput.addEventListener('keypress', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            login();
        }
    })
})

async function login() {
    const userid = document.getElementById('userid').value;
    const password = document.getElementById('password').value;
    if (userid === "" || password === "") {
        Swal.fire({
            title: '로그인 실패',
            html: '아이디 혹은 비밀번호를 입력하세요.',
            icon: 'error',
        });
        return;
    }
    const loginData = {
        id: userid,
        pw: password,
    }

    showLoading();
    Fetcher('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(loginData)
    }).then(res => {
        if (!res.ok) return;
        window.location.href = '/'
    }).catch(e => {
        Swal.fire({
            title: '로그인 실패',
            html: '아이디 혹은 비밀번호를 확인하세요.',
            icon: 'error',
        });
    }).finally(() => {
        hideLoading();
    });
}
