document.addEventListener('DOMContentLoaded', function () {
    // 사용자 정보 확인 및 관리자 메뉴 표시
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userRole = user.role || '';
    
    // 관리자 메뉴 표시/숨김
    const adminMenu = document.getElementById('adminMenu');
    if (adminMenu) {
        if (userRole === 'SUPER_ADMIN' || userRole === 'ADMIN') {
            adminMenu.style.display = 'block';
        } else {
            adminMenu.style.display = 'none';
        }
    }

    function getPath(path) {
        if (!path) return "/";
        if (path !== '/' && path.endsWith('/')) {
            path = path.slice(0, -1);
        }
        if (path === "") {
            return "/";
        }
        return path;
    }

    const currentFullUrl = window.location.href;
    const currentPathname = getPath(new URL(currentFullUrl).pathname);

    const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
    const dropdownItems = document.querySelectorAll('.navbar-nav .dropdown-item');

    // 모든 활성 상태 초기화
    navLinks.forEach(link => link.classList.remove('active'));
    dropdownItems.forEach(item => item.classList.remove('active'));
    let activeLinkSet = false;
    // 드랍다운 내 링크확인
    dropdownItems.forEach(item => {
        const itemHref = item.getAttribute('href');
        if (itemHref && itemHref !== '#') {
            if (itemHref === currentPathname) {
                item.classList.add('active');
                const parentDropdownToggle = item.closest('.nav-item.dropdown')?.querySelector('.nav-link.dropdown-toggle');
                if (parentDropdownToggle) {
                    parentDropdownToggle.classList.add('active');
                }
                activeLinkSet = true;
            }
        }
    });
    //일반 네비게이션 링크 확인
    // navLinks.forEach(link => {
    //     // 드롭다운 토글이면서 이미 자식 요소 중 활성화 된 것이 있다면 건너뜀
    //     // if (link.classList.contains('dropdown-toggle') && link.classList.contains('active')) {
    //     //     return;
    //     // }
    //     const linkHref = link.getAttribute('href');
    //     if (linkHref && linkHref !== '#') {
    //         if (linkHref === currentPathname) {
    //             link.classList.add('active');
    //             activeLinkSet = true;
    //         }
    //     }
    // });
});

async function logout() {
    Fetcher('/api/v2/auth/logout', {
        method: 'POST'
    }).then(() => {
        // localStorage에서 토큰과 사용자 정보 삭제
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        
        window.location.href = '/login';
    }).catch(error => {
        console.error('Logout error:', error);
        // 에러가 발생해도 로컬 데이터는 삭제
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        
        window.location.href = '/login';
    });
}
