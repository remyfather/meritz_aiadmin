document.addEventListener('DOMContentLoaded', function () {
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
    Fetcher('/api/auth/logout', {
        method: 'POST'
    }).then(() => window.location.href = '/login');
}
