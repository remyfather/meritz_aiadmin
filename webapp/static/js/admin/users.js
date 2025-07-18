// User Management JavaScript

let users = [];
let filteredUsers = [];
let currentPage = 1;
let usersPerPage = 10;
let userToDelete = null;

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    loadUsers();
    setupEventListeners();
});

// Setup event listeners
function setupEventListeners() {
    // Search input
    document.getElementById('searchInput').addEventListener('input', function() {
        filterUsers();
    });

    // Role filter
    document.getElementById('roleFilter').addEventListener('change', function() {
        filterUsers();
    });

    // Status filter
    document.getElementById('statusFilter').addEventListener('change', function() {
        filterUsers();
    });

    // Modal events
    const userModal = document.getElementById('userModal');
    userModal.addEventListener('hidden.bs.modal', function() {
        resetUserForm();
    });
}

// Load all users
async function loadUsers() {
    try {
        showLoading();
        const response = await Fetcher('/api/v2/admin/users');
        
        if (!response.ok) {
            throw new Error('Failed to load users');
        }
        
        users = await response.json();
        filteredUsers = [...users];
        displayUsers();
        
    } catch (error) {
        console.error('Error loading users:', error);
        Swal.fire({
            title: 'Error',
            text: 'Failed to load users: ' + error.message,
            icon: 'error',
            confirmButtonText: 'OK'
        });
    } finally {
        hideLoading();
    }
}

// Display users in table
function displayUsers() {
    const tableBody = document.getElementById('usersTableBody');
    const startIndex = (currentPage - 1) * usersPerPage;
    const endIndex = startIndex + usersPerPage;
    const pageUsers = filteredUsers.slice(startIndex, endIndex);

    if (pageUsers.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center py-4">
                    <div class="empty-state">
                        <i class="bi bi-people"></i>
                        <h5>No users found</h5>
                        <p>Try adjusting your search or filter criteria</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = pageUsers.map(user => `
        <tr>
            <td>${user.id}</td>
            <td>${highlightSearch(user.username)}</td>
            <td>${highlightSearch(user.name)}</td>
            <td>${highlightSearch(user.email)}</td>
            <td>
                <span class="role-badge role-${user.role.toLowerCase().replace('_', '-')}">
                    ${user.role}
                </span>
            </td>
            <td>
                <span class="status-badge ${user.enabled ? 'status-active' : 'status-inactive'}">
                    ${user.enabled ? 'Active' : 'Inactive'}
                </span>
            </td>
            <td>${formatDate(user.createdAt)}</td>
            <td class="action-buttons">
                <button class="btn btn-sm btn-outline-primary" onclick="editUser(${user.id})" title="Edit">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteUser(${user.id})" title="Delete">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');

    updatePagination();
}

// Filter users based on search and filters
function filterUsers() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const roleFilter = document.getElementById('roleFilter').value;
    const statusFilter = document.getElementById('statusFilter').value;

    filteredUsers = users.filter(user => {
        const matchesSearch = !searchTerm || 
            user.username.toLowerCase().includes(searchTerm) ||
            user.name.toLowerCase().includes(searchTerm) ||
            user.email.toLowerCase().includes(searchTerm);
        
        const matchesRole = !roleFilter || user.role === roleFilter;
        const matchesStatus = !statusFilter || user.enabled.toString() === statusFilter;

        return matchesSearch && matchesRole && matchesStatus;
    });

    currentPage = 1;
    displayUsers();
}

// Highlight search terms
function highlightSearch(text) {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    if (!searchTerm || !text) return text;
    
    const regex = new RegExp(`(${searchTerm})`, 'gi');
    return text.toString().replace(regex, '<span class="search-highlight">$1</span>');
}

// Update pagination
function updatePagination() {
    const totalPages = Math.ceil(filteredUsers.length / usersPerPage);
    const pagination = document.getElementById('pagination');
    
    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }

    let paginationHTML = '';
    
    // Previous button
    paginationHTML += `
        <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage - 1})">Previous</a>
        </li>
    `;

    // Page numbers
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);

    for (let i = startPage; i <= endPage; i++) {
        paginationHTML += `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" onclick="changePage(${i})">${i}</a>
            </li>
        `;
    }

    // Next button
    paginationHTML += `
        <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage + 1})">Next</a>
        </li>
    `;

    pagination.innerHTML = paginationHTML;
}

// Change page
function changePage(page) {
    const totalPages = Math.ceil(filteredUsers.length / usersPerPage);
    if (page >= 1 && page <= totalPages) {
        currentPage = page;
        displayUsers();
    }
}

// Open create user modal
function openCreateUserModal() {
    document.getElementById('userModalLabel').textContent = 'Add New User';
    document.getElementById('userId').value = '';
    document.getElementById('userForm').reset();
    document.getElementById('password').required = true;
    document.getElementById('confirmPassword').required = true;
    
    const modal = new bootstrap.Modal(document.getElementById('userModal'));
    modal.show();
}

// Edit user
async function editUser(userId) {
    try {
        showLoading();
        const response = await Fetcher(`/api/v2/admin/users/${userId}`);
        
        if (!response.ok) {
            throw new Error('Failed to load user details');
        }
        
        const user = await response.json();
        
        // Populate form
        document.getElementById('userModalLabel').textContent = 'Edit User';
        document.getElementById('userId').value = user.id;
        document.getElementById('username').value = user.username;
        document.getElementById('email').value = user.email;
        document.getElementById('name').value = user.name;
        document.getElementById('role').value = user.role;
        document.getElementById('enabled').checked = user.enabled;
        
        // Password fields are optional for editing
        document.getElementById('password').required = false;
        document.getElementById('confirmPassword').required = false;
        document.getElementById('password').value = '';
        document.getElementById('confirmPassword').value = '';
        
        const modal = new bootstrap.Modal(document.getElementById('userModal'));
        modal.show();
        
    } catch (error) {
        console.error('Error loading user:', error);
        Swal.fire({
            title: 'Error',
            text: 'Failed to load user details: ' + error.message,
            icon: 'error',
            confirmButtonText: 'OK'
        });
    } finally {
        hideLoading();
    }
}

// Save user (create or update)
async function saveUser() {
    const form = document.getElementById('userForm');
    const userId = document.getElementById('userId').value;
    
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }
    
    const userData = {
        username: document.getElementById('username').value,
        email: document.getElementById('email').value,
        name: document.getElementById('name').value,
        role: document.getElementById('role').value,
        enabled: document.getElementById('enabled').checked
    };
    
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    
    // Validate password for new users
    if (!userId && (!password || password !== confirmPassword)) {
        Swal.fire({
            title: 'Validation Error',
            text: 'Password and confirmation must match for new users',
            icon: 'error',
            confirmButtonText: 'OK'
        });
        return;
    }
    
    // Add password if provided
    if (password) {
        userData.password = password;
    }
    
    try {
        showLoading();
        
        const url = userId ? `/api/v2/admin/users/${userId}` : '/api/v2/admin/users';
        const method = userId ? 'PUT' : 'POST';
        
        const response = await Fetcher(url, {
            method: method,
            body: JSON.stringify(userData)
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to save user');
        }
        
        const savedUser = await response.json();
        
        // Close modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('userModal'));
        modal.hide();
        
        // Show success message
        Swal.fire({
            title: 'Success',
            text: userId ? 'User updated successfully' : 'User created successfully',
            icon: 'success',
            timer: 1500,
            showConfirmButton: false
        });
        
        // Reload users
        await loadUsers();
        
    } catch (error) {
        console.error('Error saving user:', error);
        Swal.fire({
            title: 'Error',
            text: 'Failed to save user: ' + error.message,
            icon: 'error',
            confirmButtonText: 'OK'
        });
    } finally {
        hideLoading();
    }
}

// Delete user
function deleteUser(userId) {
    const user = users.find(u => u.id === userId);
    userToDelete = userId;
    
    document.getElementById('deleteModalLabel').textContent = `Delete User: ${user.name}`;
    
    const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
    modal.show();
}

// Confirm delete
async function confirmDelete() {
    if (!userToDelete) return;
    
    try {
        showLoading();
        
        const response = await Fetcher(`/api/v2/admin/users/${userToDelete}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            throw new Error('Failed to delete user');
        }
        
        // Close modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('deleteModal'));
        modal.hide();
        
        // Show success message
        Swal.fire({
            title: 'Success',
            text: 'User deleted successfully',
            icon: 'success',
            timer: 1500,
            showConfirmButton: false
        });
        
        // Reload users
        await loadUsers();
        
    } catch (error) {
        console.error('Error deleting user:', error);
        Swal.fire({
            title: 'Error',
            text: 'Failed to delete user: ' + error.message,
            icon: 'error',
            confirmButtonText: 'OK'
        });
    } finally {
        hideLoading();
        userToDelete = null;
    }
}

// Reset user form
function resetUserForm() {
    document.getElementById('userForm').reset();
    document.getElementById('userId').value = '';
    document.getElementById('password').required = true;
    document.getElementById('confirmPassword').required = true;
}

// Format date
function formatDate(dateString) {
    if (!dateString) return '-';
    
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
} 