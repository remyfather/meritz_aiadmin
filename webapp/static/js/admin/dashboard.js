// 관리자 대시보드 JavaScript

class AdminDashboard {
    constructor() {
        this.currentPage = 'dashboard';
        this.init();
    }

    init() {
        this.bindEvents();
        this.loadDashboardStats();
        this.loadUsers();
        this.loadRoles();
        this.loadMenus();
    }

    bindEvents() {
        // 네비게이션 이벤트
        document.querySelectorAll('[data-page]').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                this.showPage(e.target.closest('a').dataset.page);
            });
        });

        // 로그아웃 이벤트
        document.getElementById('logoutBtn').addEventListener('click', (e) => {
            e.preventDefault();
            this.logout();
        });

        // 사용자 모달 이벤트
        document.getElementById('saveUserBtn').addEventListener('click', () => {
            this.saveUser();
        });

        // 역할 모달 이벤트
        document.getElementById('saveRoleBtn').addEventListener('click', () => {
            this.saveRole();
        });

        // 메뉴 모달 이벤트
        document.getElementById('saveMenuBtn').addEventListener('click', () => {
            this.saveMenu();
        });

        // 모달 닫힐 때 폼 초기화
        document.querySelectorAll('.modal').forEach(modal => {
            modal.addEventListener('hidden.bs.modal', () => {
                this.resetForms();
            });
        });
    }

    showPage(pageName) {
        // 모든 페이지 숨기기
        document.querySelectorAll('.page-content').forEach(page => {
            page.style.display = 'none';
        });

        // 선택된 페이지 보이기
        document.getElementById(pageName + '-page').style.display = 'block';

        // 네비게이션 활성화 상태 변경
        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.remove('active');
        });
        document.querySelector(`[data-page="${pageName}"]`).classList.add('active');

        this.currentPage = pageName;

        // 페이지별 데이터 로드
        switch (pageName) {
            case 'dashboard':
                this.loadDashboardStats();
                break;
            case 'users':
                this.loadUsers();
                break;
            case 'roles':
                this.loadRoles();
                break;
            case 'menus':
                this.loadMenus();
                break;
        }
    }

    // 대시보드 통계 로드
    async loadDashboardStats() {
        try {
            const response = await fetch('/api/v2/admin/dashboard/stats');
            const stats = await response.json();
            
            document.getElementById('totalUsers').textContent = stats.totalUsers;
            document.getElementById('enabledUsers').textContent = stats.enabledUsers;
            document.getElementById('totalRoles').textContent = stats.totalRoles;
            document.getElementById('totalMenus').textContent = stats.totalMenus;
        } catch (error) {
            console.error('대시보드 통계 로드 실패:', error);
        }
    }

    // 사용자 목록 로드
    async loadUsers() {
        try {
            const response = await fetch('/api/v2/admin/users');
            const users = await response.json();
            
            const tbody = document.querySelector('#usersTable tbody');
            tbody.innerHTML = '';
            
            users.forEach(user => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${user.id}</td>
                    <td>${user.name}</td>
                    <td>${user.username}</td>
                    <td>${user.email}</td>
                    <td><span class="badge bg-primary">${user.role}</span></td>
                    <td>
                        <span class="badge ${user.enabled ? 'bg-success' : 'bg-danger'}">
                            ${user.enabled ? '활성' : '비활성'}
                        </span>
                    </td>
                    <td>${new Date(user.createdAt).toLocaleDateString()}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="dashboard.editUser(${user.id})">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="dashboard.deleteUser(${user.id})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        } catch (error) {
            console.error('사용자 목록 로드 실패:', error);
        }
    }

    // 역할 목록 로드
    async loadRoles() {
        try {
            const response = await fetch('/api/v2/admin/roles');
            const roles = await response.json();
            
            const tbody = document.querySelector('#rolesTable tbody');
            tbody.innerHTML = '';
            
            roles.forEach(role => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${role.id}</td>
                    <td>${role.name}</td>
                    <td>${role.description || '-'}</td>
                    <td>
                        <span class="badge ${role.enabled ? 'bg-success' : 'bg-danger'}">
                            ${role.enabled ? '활성' : '비활성'}
                        </span>
                    </td>
                    <td>${new Date(role.createdAt).toLocaleDateString()}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="dashboard.editRole(${role.id})">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="dashboard.deleteRole(${role.id})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                `;
                tbody.appendChild(row);
            });

            // 사용자 모달의 역할 선택 옵션 업데이트
            this.updateRoleOptions();
        } catch (error) {
            console.error('역할 목록 로드 실패:', error);
        }
    }

    // 메뉴 목록 로드
    async loadMenus() {
        try {
            const response = await fetch('/api/v2/admin/menus');
            const menus = await response.json();
            
            const tbody = document.querySelector('#menusTable tbody');
            tbody.innerHTML = '';
            
            menus.forEach(menu => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${menu.id}</td>
                    <td>${menu.name}</td>
                    <td>${menu.url || '-'}</td>
                    <td><i class="${menu.icon || 'bi bi-list'}"></i></td>
                    <td>${menu.sortOrder}</td>
                    <td>
                        <span class="badge ${menu.enabled ? 'bg-success' : 'bg-danger'}">
                            ${menu.enabled ? '활성' : '비활성'}
                        </span>
                    </td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="dashboard.editMenu(${menu.id})">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="dashboard.deleteMenu(${menu.id})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        } catch (error) {
            console.error('메뉴 목록 로드 실패:', error);
        }
    }

    // 사용자 저장
    async saveUser() {
        const userId = document.getElementById('userId').value;
        const userData = {
            name: document.getElementById('userName').value,
            username: document.getElementById('userUsername').value,
            email: document.getElementById('userEmail').value,
            role: document.getElementById('userRole').value,
            enabled: document.getElementById('userEnabled').checked
        };

        try {
            const url = userId ? `/api/v2/admin/users/${userId}` : '/api/v2/admin/users';
            const method = userId ? 'PUT' : 'POST';
            
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            });

            if (response.ok) {
                this.showAlert('사용자가 성공적으로 저장되었습니다.', 'success');
                bootstrap.Modal.getInstance(document.getElementById('userModal')).hide();
                this.loadUsers();
            } else {
                throw new Error('사용자 저장 실패');
            }
        } catch (error) {
            console.error('사용자 저장 실패:', error);
            this.showAlert('사용자 저장에 실패했습니다.', 'danger');
        }
    }

    // 역할 저장
    async saveRole() {
        const roleId = document.getElementById('roleId').value;
        const roleData = {
            name: document.getElementById('roleName').value,
            description: document.getElementById('roleDescription').value,
            enabled: document.getElementById('roleEnabled').checked
        };

        try {
            const url = roleId ? `/api/v2/admin/roles/${roleId}` : '/api/v2/admin/roles';
            const method = roleId ? 'PUT' : 'POST';
            
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(roleData)
            });

            if (response.ok) {
                this.showAlert('역할이 성공적으로 저장되었습니다.', 'success');
                bootstrap.Modal.getInstance(document.getElementById('roleModal')).hide();
                this.loadRoles();
            } else {
                throw new Error('역할 저장 실패');
            }
        } catch (error) {
            console.error('역할 저장 실패:', error);
            this.showAlert('역할 저장에 실패했습니다.', 'danger');
        }
    }

    // 메뉴 저장
    async saveMenu() {
        const menuId = document.getElementById('menuId').value;
        const menuData = {
            name: document.getElementById('menuName').value,
            url: document.getElementById('menuUrl').value,
            icon: document.getElementById('menuIcon').value,
            sortOrder: parseInt(document.getElementById('menuSortOrder').value) || 0,
            enabled: document.getElementById('menuEnabled').checked
        };

        try {
            const url = menuId ? `/api/v2/admin/menus/${menuId}` : '/api/v2/admin/menus';
            const method = menuId ? 'PUT' : 'POST';
            
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(menuData)
            });

            if (response.ok) {
                this.showAlert('메뉴가 성공적으로 저장되었습니다.', 'success');
                bootstrap.Modal.getInstance(document.getElementById('menuModal')).hide();
                this.loadMenus();
            } else {
                throw new Error('메뉴 저장 실패');
            }
        } catch (error) {
            console.error('메뉴 저장 실패:', error);
            this.showAlert('메뉴 저장에 실패했습니다.', 'danger');
        }
    }

    // 사용자 편집
    async editUser(id) {
        try {
            const response = await fetch(`/api/v2/admin/users/${id}`);
            const user = await response.json();
            
            document.getElementById('userId').value = user.id;
            document.getElementById('userName').value = user.name;
            document.getElementById('userUsername').value = user.username;
            document.getElementById('userEmail').value = user.email;
            document.getElementById('userRole').value = user.role;
            document.getElementById('userEnabled').checked = user.enabled;
            
            document.getElementById('userModalTitle').textContent = '사용자 편집';
            
            const modal = new bootstrap.Modal(document.getElementById('userModal'));
            modal.show();
        } catch (error) {
            console.error('사용자 정보 로드 실패:', error);
            this.showAlert('사용자 정보를 불러오는데 실패했습니다.', 'danger');
        }
    }

    // 역할 편집
    async editRole(id) {
        try {
            const response = await fetch(`/api/v2/admin/roles/${id}`);
            const role = await response.json();
            
            document.getElementById('roleId').value = role.id;
            document.getElementById('roleName').value = role.name;
            document.getElementById('roleDescription').value = role.description || '';
            document.getElementById('roleEnabled').checked = role.enabled;
            
            document.getElementById('roleModalTitle').textContent = '역할 편집';
            
            const modal = new bootstrap.Modal(document.getElementById('roleModal'));
            modal.show();
        } catch (error) {
            console.error('역할 정보 로드 실패:', error);
            this.showAlert('역할 정보를 불러오는데 실패했습니다.', 'danger');
        }
    }

    // 메뉴 편집
    async editMenu(id) {
        try {
            const response = await fetch(`/api/v2/admin/menus/${id}`);
            const menu = await response.json();
            
            document.getElementById('menuId').value = menu.id;
            document.getElementById('menuName').value = menu.name;
            document.getElementById('menuUrl').value = menu.url || '';
            document.getElementById('menuIcon').value = menu.icon || '';
            document.getElementById('menuSortOrder').value = menu.sortOrder || 0;
            document.getElementById('menuEnabled').checked = menu.enabled;
            
            document.getElementById('menuModalTitle').textContent = '메뉴 편집';
            
            const modal = new bootstrap.Modal(document.getElementById('menuModal'));
            modal.show();
        } catch (error) {
            console.error('메뉴 정보 로드 실패:', error);
            this.showAlert('메뉴 정보를 불러오는데 실패했습니다.', 'danger');
        }
    }

    // 사용자 삭제
    async deleteUser(id) {
        if (confirm('정말로 이 사용자를 삭제하시겠습니까?')) {
            try {
                const response = await fetch(`/api/v2/admin/users/${id}`, {
                    method: 'DELETE'
                });

                if (response.ok) {
                    this.showAlert('사용자가 성공적으로 삭제되었습니다.', 'success');
                    this.loadUsers();
                } else {
                    throw new Error('사용자 삭제 실패');
                }
            } catch (error) {
                console.error('사용자 삭제 실패:', error);
                this.showAlert('사용자 삭제에 실패했습니다.', 'danger');
            }
        }
    }

    // 역할 삭제
    async deleteRole(id) {
        if (confirm('정말로 이 역할을 삭제하시겠습니까?')) {
            try {
                const response = await fetch(`/api/v2/admin/roles/${id}`, {
                    method: 'DELETE'
                });

                if (response.ok) {
                    this.showAlert('역할이 성공적으로 삭제되었습니다.', 'success');
                    this.loadRoles();
                } else {
                    throw new Error('역할 삭제 실패');
                }
            } catch (error) {
                console.error('역할 삭제 실패:', error);
                this.showAlert('역할 삭제에 실패했습니다.', 'danger');
            }
        }
    }

    // 메뉴 삭제
    async deleteMenu(id) {
        if (confirm('정말로 이 메뉴를 삭제하시겠습니까?')) {
            try {
                const response = await fetch(`/api/v2/admin/menus/${id}`, {
                    method: 'DELETE'
                });

                if (response.ok) {
                    this.showAlert('메뉴가 성공적으로 삭제되었습니다.', 'success');
                    this.loadMenus();
                } else {
                    throw new Error('메뉴 삭제 실패');
                }
            } catch (error) {
                console.error('메뉴 삭제 실패:', error);
                this.showAlert('메뉴 삭제에 실패했습니다.', 'danger');
            }
        }
    }

    // 역할 옵션 업데이트
    async updateRoleOptions() {
        try {
            const response = await fetch('/api/v2/admin/roles');
            const roles = await response.json();
            
            const select = document.getElementById('userRole');
            select.innerHTML = '';
            
            roles.forEach(role => {
                const option = document.createElement('option');
                option.value = role.name;
                option.textContent = role.name;
                select.appendChild(option);
            });
        } catch (error) {
            console.error('역할 옵션 업데이트 실패:', error);
        }
    }

    // 폼 초기화
    resetForms() {
        document.getElementById('userForm').reset();
        document.getElementById('roleForm').reset();
        document.getElementById('menuForm').reset();
        
        document.getElementById('userId').value = '';
        document.getElementById('roleId').value = '';
        document.getElementById('menuId').value = '';
        
        document.getElementById('userModalTitle').textContent = '새 사용자';
        document.getElementById('roleModalTitle').textContent = '새 역할';
        document.getElementById('menuModalTitle').textContent = '새 메뉴';
    }

    // 알림 표시
    showAlert(message, type) {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
        alertDiv.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(alertDiv);
        
        // 3초 후 자동 제거
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 3000);
    }

    // 로그아웃
    logout() {
        if (confirm('로그아웃하시겠습니까?')) {
            // 세션/토큰 제거
            localStorage.removeItem('token');
            sessionStorage.clear();
            
            // 로그인 페이지로 이동
            window.location.href = '/login';
        }
    }
}

// 페이지 로드 시 대시보드 초기화
document.addEventListener('DOMContentLoaded', () => {
    window.dashboard = new AdminDashboard();
}); 