// Javascript for Student Management System SaaS Interface - Sprints 8-19 Complete Integration

// Global Application State
const STATE = {
    token: null,
    username: null,
    role: null,
    students: [],      // Local cache of students fetched from backend
    departments: {
        1: "Computer Science & Eng",
        2: "Mechanical Engineering",
        3: "Business Administration"
    },
    activeTab: 'dashboard',
    directory: {
        currentPage: 1,
        pageSize: 5,
        sortField: 'name',
        sortOrder: 'asc',
        filterDept: 'ALL',
        filterStatus: 'ACTIVE',
        searchQuery: ''
    },
    addStudentStep: 1
};

document.addEventListener('DOMContentLoaded', () => {
    // Initialize Theme
    initTheme();

    // Check Authentication
    checkAuth();

    // Setup Event Listeners
    initAppEvents();
});

/* =========================================================================
   Theme Management (Light / Dark Mode)
   ========================================================================= */
function initTheme() {
    const themeToggleBtn = document.getElementById('btn-theme-toggle');
    const themeIcon = document.getElementById('theme-icon');
    
    let currentTheme = localStorage.getItem('sms-theme') || 'dark';
    document.documentElement.setAttribute('data-theme', currentTheme);
    updateThemeUI(currentTheme);

    themeToggleBtn.addEventListener('click', () => {
        let activeTheme = document.documentElement.getAttribute('data-theme');
        let newTheme = activeTheme === 'dark' ? 'light' : 'dark';
        
        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('sms-theme', newTheme);
        
        themeIcon.classList.add('rotate-in');
        setTimeout(() => themeIcon.classList.remove('rotate-in'), 300);
        updateThemeUI(newTheme);
    });
}

function updateThemeUI(theme) {
    const themeIcon = document.getElementById('theme-icon');
    if (!themeIcon) return;
    if (theme === 'dark') {
        themeIcon.className = 'bi bi-sun-fill';
    } else {
        themeIcon.className = 'bi bi-moon-stars-fill';
    }
}

/* =========================================================================
   Authentication Gateway & Session Handling (Sprint 14)
   ========================================================================= */
function checkAuth() {
    const token = localStorage.getItem('sms-jwt-token');
    const username = localStorage.getItem('sms-username');
    const role = localStorage.getItem('sms-role');
    
    const loginOverlay = document.getElementById('login-overlay');
    const appContainer = document.getElementById('app-wrapper-container');

    if (token) {
        STATE.token = token;
        STATE.username = username;
        STATE.role = role;

        // Populate User elements in Navbar
        document.getElementById('profile-name-display').textContent = username;
        document.getElementById('profile-role-display').textContent = role === 'ROLE_ADMIN' ? 'Registrar Administrator' : 'Student User';
        
        // Hide login and show dashboard
        loginOverlay.classList.add('d-none');
        appContainer.classList.remove('d-none');

        // Role-based visibility controls (Sprints 14/19 Admin vs Student separation)
        const menuAdd = document.getElementById('menu-add-student');
        const menuDiag = document.getElementById('menu-diagnostics');
        const menuFac = document.getElementById('menu-faculties-mgmt');
        const quickAddBtn = document.getElementById('btn-quick-add-student');

        if (role === 'ROLE_ADMIN') {
            if (menuAdd) menuAdd.classList.remove('d-none');
            if (menuDiag) menuDiag.classList.remove('d-none');
            if (menuFac) menuFac.classList.remove('d-none');
            if (quickAddBtn) quickAddBtn.classList.remove('d-none');
            STATE.directory.adminAccess = true;
        } else { // ROLE_STUDENT
            if (menuAdd) menuAdd.classList.add('d-none');
            if (menuDiag) menuDiag.classList.add('d-none');
            if (menuFac) menuFac.classList.add('d-none');
            if (quickAddBtn) quickAddBtn.classList.add('d-none');
            STATE.directory.adminAccess = false;
            
            // If active tab is admin-only, redirect to dashboard
            if (STATE.activeTab === 'add-student' || STATE.activeTab === 'diagnostics' || STATE.activeTab === 'faculties-mgmt') {
                switchTab('dashboard');
            }
        }
        
        // Initial telemetry loaders
        loadDashboardData();
        loadStudentDirectory();
    } else {
        // Show login overlay
        loginOverlay.classList.remove('d-none');
        appContainer.classList.add('d-none');
    }
}

// Binds login form submit
function initAppEvents() {
    // Login Submit
    const loginForm = document.getElementById('login-form');
    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        handleLogin();
    });

    // Logout Click
    const logoutBtn = document.getElementById('btn-auth-logout');
    logoutBtn.addEventListener('click', (e) => {
        e.preventDefault();
        handleLogout();
    });

    // Sidebar Tabbing
    initTabs();

    // Collapsible Sidebars
    initSidebar();

    // Diagnostics poller
    initDiagnostics();

    // Load directory actions
    initDirectoryControls();

    // Load Form Wizards
    initFormWizards();

    // Interactive dashboard stats cards
    initStatsCardsClickEvents();
}

function handleLogin() {
    const usernameInput = document.getElementById('login-username').value;
    const passwordInput = document.getElementById('login-password').value;
    const errorAlert = document.getElementById('login-error-alert');
    const spinner = document.getElementById('login-spinner');
    
    errorAlert.classList.add('d-none');
    spinner.classList.remove('d-none');

    fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: usernameInput, password: passwordInput })
    })
    .then(res => {
        if (!res.ok) {
            throw new Error("Invalid username or password credentials.");
        }
        return res.json();
    })
    .then(data => {
        // Store session tokens
        localStorage.setItem('sms-jwt-token', data.token);
        localStorage.setItem('sms-username', data.username);
        localStorage.setItem('sms-role', data.role);

        showToast(`Successfully signed in as ${data.username}`, "success");
        checkAuth();
    })
    .catch(err => {
        errorAlert.textContent = err.message || "Failed to authenticate.";
        errorAlert.classList.remove('d-none');
    })
    .finally(() => {
        spinner.classList.add('d-none');
    });
}

function handleLogout() {
    localStorage.removeItem('sms-jwt-token');
    localStorage.removeItem('sms-username');
    localStorage.removeItem('sms-role');
    STATE.token = null;
    showToast("Logged out successfully.", "warning");
    checkAuth();
}

// Fetch helper injecting JWT token authorization header automatically
function authorizedFetch(url, options = {}) {
    if (!options.headers) options.headers = {};
    if (STATE.token) {
        options.headers['Authorization'] = `Bearer ${STATE.token}`;
    }
    return fetch(url, options).then(res => {
        if (res.status === 401) {
            handleLogout();
            throw new Error("Session expired. Please log in again.");
        }
        return res;
    });
}

/* =========================================================================
   Sidebar & SPA Tabs Navigation
   ========================================================================= */
function initSidebar() {
    const sidebar = document.getElementById('sidebar');
    const collapseBtn = document.getElementById('btn-collapse-sidebar');
    const collapseIcon = document.getElementById('collapse-icon');
    const mobileBtn = document.getElementById('btn-mobile-sidebar');

    if (collapseBtn) {
        collapseBtn.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
            collapseIcon.className = sidebar.classList.contains('collapsed') ? 'bi bi-chevron-right' : 'bi bi-chevron-left';
            setTimeout(animateChart, 300);
        });
    }

    if (mobileBtn) {
        mobileBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            sidebar.classList.toggle('mobile-show');
        });
    }

    document.addEventListener('click', (e) => {
        if (window.innerWidth < 992 && sidebar.classList.contains('mobile-show') && !sidebar.contains(e.target)) {
            sidebar.classList.remove('mobile-show');
        }
    });
}

function initTabs() {
    const menuItems = document.querySelectorAll('.sidebar-menu .menu-item');
    const sections = document.querySelectorAll('.view-section');

    menuItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const tabName = item.getAttribute('data-tab');
            switchTab(tabName);
        });
    });
}

function switchTab(tabName) {
    const menuItems = document.querySelectorAll('.sidebar-menu .menu-item');
    const sections = document.querySelectorAll('.view-section');
    const sidebar = document.getElementById('sidebar');

    menuItems.forEach(mi => {
        mi.classList.remove('active');
        if (mi.getAttribute('data-tab') === tabName) mi.classList.add('active');
    });

    sidebar.classList.remove('mobile-show');
    STATE.activeTab = tabName;

    sections.forEach(section => {
        section.classList.remove('active');
        if (section.id === `view-${tabName}`) {
            section.classList.add('active');
            
            // Tab specific triggers
            if (tabName === 'dashboard') {
                loadDashboardData();
                animateChart();
            } else if (tabName === 'students-list') {
                loadStudentDirectory();
            } else if (tabName === 'faculties-mgmt') {
                loadFacultyPageData();
            }
        }
    });
}

/* =========================================================================
   Dashboard Widgets & Telemetry (Sprint 13)
   ========================================================================= */
function loadDashboardData() {
    authorizedFetch('/api/students')
        .then(res => res.json())
        .then(data => {
            // Update widget metrics
            document.getElementById('dash-total-students').textContent = data.length;
            document.getElementById('dash-total-depts').textContent = 3;

            // Load recent registrations list
            const recentTbody = document.getElementById('dashboard-recent-tbody');
            recentTbody.innerHTML = '';
            
            // Sort by registration date or id descending
            const sorted = [...data].sort((a,b) => b.id - a.id).slice(0, 3);
            
            if (sorted.length === 0) {
                recentTbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No student profiles registered yet.</td></tr>';
                return;
            }

            sorted.forEach(student => {
                const tr = document.createElement('tr');
                const avatar = student.photoPath || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=50';
                tr.innerHTML = `
                    <td>
                        <div class="avatar-cell">
                            <img src="${avatar}" alt="Avatar">
                            <div>
                                <div class="fw-bold text-white">${student.firstName} ${student.lastName}</div>
                                <span class="text-muted small">Registered</span>
                            </div>
                        </div>
                    </td>
                    <td><code class="text-info fw-bold">${student.studentNumber}</code></td>
                    <td>${student.email}</td>
                    <td><span class="badge-custom blue">${student.departmentName || 'N/A'}</span></td>
                    <td>${new Date(student.createdAt).toLocaleDateString()}</td>
                `;
                recentTbody.appendChild(tr);
            });

            // Render monthly registrations dynamically
            renderRegistrationsChart(data);
        })
        .catch(err => logError("Failed to fetch dashboard registry statistics", err));

    // Load health diagnostics telemetry uptime
    authorizedFetch('/api/health')
        .then(res => res.json())
        .then(data => {
            document.getElementById('dash-uptime-text').textContent = formatUptimeString(data.uptimeMs);
        })
        .catch(() => {
            document.getElementById('dash-uptime-text').textContent = 'OFFLINE';
        });
}

function renderRegistrationsChart(students) {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul'];
    const currentYear = new Date().getFullYear();
    
    // Initialize monthly counts
    const monthlyCounts = Array(7).fill(0);
    
    students.forEach(student => {
        const date = new Date(student.createdAt);
        const monthIdx = date.getMonth(); // 0 = Jan, 11 = Dec
        if (monthIdx >= 0 && monthIdx < 7) {
            monthlyCounts[monthIdx]++;
        }
    });

    const maxCount = Math.max(...monthlyCounts, 1);
    
    const chartContainer = document.getElementById('registrations-mock-chart');
    if (!chartContainer) return;
    
    chartContainer.innerHTML = '';
    months.forEach((month, idx) => {
        const count = monthlyCounts[idx];
        // Height scale between 10% (empty baseline) and 85%
        const heightPct = count === 0 ? 10 : Math.round((count / maxCount) * 75) + 10;
        
        const barWrapper = document.createElement('div');
        barWrapper.className = 'chart-bar-wrapper';
        barWrapper.innerHTML = `
            <div class="chart-bar" style="height: 0px;" data-val="${heightPct}%" title="${count} registrations">
                <span class="chart-bar-tooltip">${count} students</span>
            </div>
            <div class="chart-label">${month}</div>
        `;
        chartContainer.appendChild(barWrapper);
    });
    
    animateChart();
}

function animateChart() {
    const bars = document.querySelectorAll('#registrations-mock-chart .chart-bar');
    bars.forEach(bar => {
        const val = bar.getAttribute('data-val') || '10%';
        bar.style.height = '0';
        setTimeout(() => {
            bar.style.height = val;
        }, 100);
    });
}

/* =========================================================================
   Student List Directory & Pagination (Sprint 7 & 17)
   ========================================================================= */
function loadStudentDirectory() {
    const loader = document.getElementById('students-table-skeleton');
    const table = document.getElementById('students-table-data');
    
    loader.classList.remove('d-none');
    table.classList.add('d-none');

    // Handle soft delete filter query mapping
    const endpoint = STATE.directory.filterStatus === 'DELETED' ? '/api/students/deleted' : '/api/students';

    authorizedFetch(endpoint)
        .then(res => res.json())
        .then(data => {
            STATE.students = data;
            applyDirectoryFilteringAndRendering();
        })
        .catch(err => {
            showToast("Could not load student list database.", "error");
        })
        .finally(() => {
            setTimeout(() => {
                loader.classList.add('d-none');
                table.classList.remove('d-none');
            }, 300);
        });
}

function initDirectoryControls() {
    const refreshBtn = document.getElementById('btn-refresh-students');
    const deptFilter = document.getElementById('filter-department');
    const statusFilter = document.getElementById('filter-status');
    const clearBtn = document.getElementById('btn-clear-filters');
    const sortHeaders = document.querySelectorAll('.sort-header');
    
    // Search inputs (navbar & directory) with debouncing (Sprint 12)
    const searchInput = document.getElementById('student-search-input');
    const navSearchInput = document.getElementById('nav-global-search');

    if (refreshBtn) refreshBtn.addEventListener('click', loadStudentDirectory);
    if (deptFilter) {
        deptFilter.addEventListener('change', () => {
            STATE.directory.filterDept = deptFilter.value;
            applyDirectoryFilteringAndRendering();
        });
    }
    if (statusFilter) {
        statusFilter.addEventListener('change', () => {
            STATE.directory.filterStatus = statusFilter.value;
            loadStudentDirectory();
        });
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', (e) => {
            e.preventDefault();
            searchInput.value = '';
            navSearchInput.value = '';
            deptFilter.value = 'ALL';
            statusFilter.value = 'ACTIVE';
            STATE.directory.searchQuery = '';
            STATE.directory.filterDept = 'ALL';
            STATE.directory.filterStatus = 'ACTIVE';
            loadStudentDirectory();
        });
    }

    // Debounced search (Sprint 12)
    const debouncedFilter = debounce(() => {
        applyDirectoryFilteringAndRendering();
    }, 300);

    if (searchInput) {
        searchInput.addEventListener('input', () => {
            STATE.directory.searchQuery = searchInput.value;
            debouncedFilter();
        });
    }
    if (navSearchInput) {
        navSearchInput.addEventListener('input', () => {
            STATE.directory.searchQuery = navSearchInput.value;
            if (STATE.activeTab !== 'students-list') {
                switchTab('students-list');
            }
            searchInput.value = navSearchInput.value;
            debouncedFilter();
        });
    }

    // Sort column headers
    sortHeaders.forEach(header => {
        header.addEventListener('click', () => {
            const field = header.getAttribute('data-sort');
            if (STATE.directory.sortField === field) {
                STATE.directory.sortOrder = STATE.directory.sortOrder === 'asc' ? 'desc' : 'asc';
            } else {
                STATE.directory.sortField = field;
                STATE.directory.sortOrder = 'asc';
            }
            
            // Cycle visual icons
            sortHeaders.forEach(sh => {
                const icon = sh.querySelector('i');
                icon.className = 'bi bi-arrow-down-up';
            });
            const currentIcon = header.querySelector('i');
            currentIcon.className = STATE.directory.sortOrder === 'asc' ? 'bi bi-sort-alpha-down text-info' : 'bi bi-sort-alpha-up text-info';

            applyDirectoryFilteringAndRendering();
        });
    });

    // Dynamic document exporters headers mapping token values (Sprint 16)
    document.getElementById('export-excel-btn').addEventListener('click', (e) => injectTokenToDownload(e));
    document.getElementById('export-pdf-btn').addEventListener('click', (e) => injectTokenToDownload(e));
    document.getElementById('export-csv-btn').addEventListener('click', (e) => injectTokenToDownload(e));

    // Print ledger trigger
    const printBtn = document.getElementById('btn-print-register');
    if (printBtn) {
        printBtn.addEventListener('click', (e) => {
            e.preventDefault();
            window.print();
        });
    }
}

// Redirects security bypass download files attaching tokens inside URL params since browser fetch cannot handle anchor redirects
function injectTokenToDownload(e) {
    e.preventDefault();
    const anchor = e.currentTarget;
    const baseUrl = anchor.getAttribute('href');
    window.open(`${baseUrl}?access_token=${STATE.token}`, '_blank');
}

function applyDirectoryFilteringAndRendering() {
    const tbody = document.getElementById('students-directory-tbody');
    tbody.innerHTML = '';

    // 1. Filtering
    let filtered = STATE.students.filter(student => {
        // Search query
        const query = STATE.directory.searchQuery.toLowerCase().trim();
        const searchMatches = query === '' || 
            student.firstName.toLowerCase().includes(query) ||
            student.lastName.toLowerCase().includes(query) ||
            student.studentNumber.toLowerCase().includes(query) ||
            student.email.toLowerCase().includes(query) ||
            (student.phone && student.phone.includes(query));

        // Department
        const deptMatches = STATE.directory.filterDept === 'ALL' || 
            student.departmentId.toString() === STATE.directory.filterDept;

        return searchMatches && deptMatches;
    });

    // 2. Sorting
    filtered.sort((a, b) => {
        let valA = '', valB = '';
        if (STATE.directory.sortField === 'name') {
            valA = `${a.firstName} ${a.lastName}`.toLowerCase();
            valB = `${b.firstName} ${b.lastName}`.toLowerCase();
        } else if (STATE.directory.sortField === 'id') {
            valA = a.studentNumber.toLowerCase();
            valB = b.studentNumber.toLowerCase();
        } else if (STATE.directory.sortField === 'dept') {
            valA = (a.departmentName || '').toLowerCase();
            valB = (b.departmentName || '').toLowerCase();
        }

        if (valA < valB) return STATE.directory.sortOrder === 'asc' ? -1 : 1;
        if (valA > valB) return STATE.directory.sortOrder === 'asc' ? 1 : -1;
        return 0;
    });

    // Update count labels
    const countLabel = document.getElementById('directory-count-text');
    const isDel = STATE.directory.filterStatus === 'DELETED';
    countLabel.textContent = `Showing ${filtered.length} ${isDel ? 'soft-deleted' : 'active'} student records`;

    // 3. Pagination slicing
    const totalRecords = filtered.length;
    const totalPages = Math.ceil(totalRecords / STATE.directory.pageSize) || 1;
    
    // Boundaries checks
    if (STATE.directory.currentPage > totalPages) STATE.directory.currentPage = totalPages;
    if (STATE.directory.currentPage < 1) STATE.directory.currentPage = 1;

    const startIndex = (STATE.directory.currentPage - 1) * STATE.directory.pageSize;
    const endIndex = Math.min(startIndex + STATE.directory.pageSize, totalRecords);
    
    const paginated = filtered.slice(startIndex, endIndex);

    // Update Pagination Footer Details
    const infoText = document.getElementById('pagination-info-text');
    infoText.textContent = totalRecords === 0 ? "Showing 0 to 0 of 0 records" : `Showing ${startIndex + 1} to ${endIndex} of ${totalRecords} records`;

    renderPaginationControls(totalPages);

    // 4. Render Table rows
    if (paginated.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No matching records found.</td></tr>`;
        return;
    }

    paginated.forEach(student => {
        const tr = document.createElement('tr');
        const isSoftDeleted = student.deletedAt !== null;
        if (isSoftDeleted) tr.className = 'row-deleted';

        const avatar = student.photoPath || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=50';
        
        // Status badge
        let statusBadge = `<span class="badge-custom green"><i class="bi bi-check-circle-fill me-1"></i>Active</span>`;
        let actionButtons = '';

        if (STATE.directory.adminAccess) {
            actionButtons = `
                <div class="d-flex gap-1">
                    <button class="btn btn-dark btn-sm border-secondary text-secondary btn-view-details" data-id="${student.id}" title="View details"><i class="bi bi-eye"></i></button>
                    <button class="btn btn-dark btn-sm border-secondary text-secondary btn-edit-student" data-id="${student.id}" title="Edit Profile"><i class="bi bi-pencil"></i></button>
                    <button class="btn btn-dark btn-sm border-secondary text-danger btn-delete-student" data-id="${student.id}" title="Soft Delete"><i class="bi bi-trash"></i></button>
                </div>
            `;
            if (isSoftDeleted) {
                statusBadge = `<span class="badge-custom red"><i class="bi bi-x-circle-fill me-1"></i>Deleted</span>`;
                actionButtons = `
                    <div class="d-flex gap-1">
                        <button class="btn btn-dark btn-sm border-secondary text-secondary" disabled><i class="bi bi-eye"></i></button>
                        <button class="btn btn-dark btn-sm border-secondary text-secondary" disabled><i class="bi bi-pencil"></i></button>
                        <button class="btn btn-success btn-sm border-secondary text-white btn-restore-student" data-id="${student.id}" title="Restore Record"><i class="bi bi-arrow-counterclockwise"></i></button>
                    </div>
                `;
            }
        } else {
            // Student read-only mode - only view details allowed!
            actionButtons = `
                <div class="d-flex gap-1">
                    <button class="btn btn-dark btn-sm border-secondary text-secondary btn-view-details" data-id="${student.id}" title="View details"><i class="bi bi-eye"></i></button>
                </div>
            `;
            if (isSoftDeleted) {
                statusBadge = `<span class="badge-custom red"><i class="bi bi-x-circle-fill me-1"></i>Deleted</span>`;
                actionButtons = `
                    <div class="d-flex gap-1">
                        <button class="btn btn-dark btn-sm border-secondary text-secondary" disabled><i class="bi bi-eye"></i></button>
                    </div>
                `;
            }
        }

        tr.innerHTML = `
            <td>
                <div class="avatar-cell">
                    <img src="${avatar}" alt="Avatar">
                    <div>
                        <div class="fw-bold text-white ${isSoftDeleted ? 'text-decoration-line-through' : ''}">${student.firstName} ${student.lastName}</div>
                        <span class="text-muted small">Registered</span>
                    </div>
                </div>
            </td>
            <td><code class="${isSoftDeleted ? 'text-secondary text-decoration-line-through' : 'text-info'} fw-bold">${student.studentNumber}</code></td>
            <td class="${isSoftDeleted ? 'text-muted text-decoration-line-through' : ''}">${student.email}</td>
            <td><span class="badge-custom blue ${isSoftDeleted ? 'opacity-50' : ''}">${student.departmentName || 'N/A'}</span></td>
            <td>${statusBadge}</td>
            <td>${actionButtons}</td>
        `;
        tbody.appendChild(tr);
    });

    // Bind action events dynamically
    tbody.querySelectorAll('.btn-view-details').forEach(btn => {
        btn.addEventListener('click', () => showStudentDetails(btn.getAttribute('data-id')));
    });

    tbody.querySelectorAll('.btn-edit-student').forEach(btn => {
        btn.addEventListener('click', () => showEditStudentForm(btn.getAttribute('data-id')));
    });

    tbody.querySelectorAll('.btn-delete-student').forEach(btn => {
        btn.addEventListener('click', () => handleSoftDeleteStudent(btn.getAttribute('data-id')));
    });

    tbody.querySelectorAll('.btn-restore-student').forEach(btn => {
        btn.addEventListener('click', () => handleRestoreStudent(btn.getAttribute('data-id')));
    });
}

function renderPaginationControls(totalPages) {
    const container = document.getElementById('pagination-buttons-container');
    container.innerHTML = '';

    // Prev Button
    const prev = document.createElement('a');
    prev.href = '#';
    prev.className = `btn-pagination ${STATE.directory.currentPage === 1 ? 'disabled' : ''}`;
    prev.innerHTML = `<i class="bi bi-chevron-left"></i>`;
    if (STATE.directory.currentPage > 1) {
        prev.addEventListener('click', (e) => {
            e.preventDefault();
            STATE.directory.currentPage--;
            applyDirectoryFilteringAndRendering();
        });
    }
    container.appendChild(prev);

    // Number Pages
    for (let i = 1; i <= totalPages; i++) {
        const num = document.createElement('a');
        num.href = '#';
        num.className = `page-num ${STATE.directory.currentPage === i ? 'active' : ''}`;
        num.textContent = i;
        num.addEventListener('click', (e) => {
            e.preventDefault();
            STATE.directory.currentPage = i;
            applyDirectoryFilteringAndRendering();
        });
        container.appendChild(num);
    }

    // Next Button
    const next = document.createElement('a');
    next.href = '#';
    next.className = `btn-pagination ${STATE.directory.currentPage === totalPages ? 'disabled' : ''}`;
    next.innerHTML = `<i class="bi bi-chevron-right"></i>`;
    if (STATE.directory.currentPage < totalPages) {
        next.addEventListener('click', (e) => {
            e.preventDefault();
            STATE.directory.currentPage++;
            applyDirectoryFilteringAndRendering();
        });
    }
    container.appendChild(next);
}

function handleSoftDeleteStudent(id) {
    if (confirm("Are you sure you want to soft delete this student profile?")) {
        authorizedFetch(`/api/students/${id}`, { method: 'DELETE' })
            .then(res => {
                if (!res.ok) throw new Error("Delete failed.");
                showToast("Student profile successfully soft-deleted.", "warning");
                loadStudentDirectory();
            })
            .catch(err => showToast(err.message, "error"));
    }
}

function handleRestoreStudent(id) {
    authorizedFetch(`/api/students/${id}/restore`, { method: 'PUT' })
        .then(res => {
            if (!res.ok) throw new Error("Restoration failed.");
            showToast("Student profile restored back to active registry status.", "success");
            loadStudentDirectory();
        })
        .catch(err => showToast(err.message, "error"));
}

/* =========================================================================
   Add Student Step Wizard (Sprint 8 & 15 Photo Upload)
   ========================================================================= */
function initFormWizards() {
    const nextBtn = document.getElementById('btn-add-step-next');
    const prevBtn = document.getElementById('btn-add-step-prev');
    const addForm = document.getElementById('add-student-form');
    
    // File inputs preview events
    const photoFileInput = document.getElementById('add-photo-file');
    if (photoFileInput) {
        photoFileInput.addEventListener('change', () => {
            const file = photoFileInput.files[0];
            if (file) {
                document.getElementById('add-file-name-label').textContent = `${file.name} (${(file.size / (1024*1024)).toFixed(2)} MB)`;
                
                const reader = new FileReader();
                reader.onload = (e) => {
                    document.getElementById('add-avatar-preview').src = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            if (STATE.addStudentStep < 4) {
                // Validate current step before proceeding
                if (validateAddStep(STATE.addStudentStep)) {
                    STATE.addStudentStep++;
                    updateAddStepUI();
                }
            } else {
                // Submit multi-step form
                submitAddStudentForm();
            }
        });
    }

    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            if (STATE.addStudentStep > 1) {
                STATE.addStudentStep--;
                updateAddStepUI();
            }
        });
    }
}

function updateAddStepUI() {
    const nextBtn = document.getElementById('btn-add-step-next');
    const nextIcon = document.getElementById('btn-add-step-icon');
    const prevBtn = document.getElementById('btn-add-step-prev');
    const progressBar = document.getElementById('add-step-progress-bar');

    // Toggle content panes
    for (let i = 1; i <= 4; i++) {
        const pane = document.getElementById(`add-step-${i}-content`);
        const node = document.getElementById(`node-step-${i}`);
        
        pane.classList.remove('active');
        node.className = 'step-node';

        if (i === STATE.addStudentStep) {
            pane.classList.add('active');
            node.classList.add('active');
        } else if (i < STATE.addStudentStep) {
            node.classList.add('completed');
        }
    }

    // Toggle button visibilities
    prevBtn.disabled = STATE.addStudentStep === 1;
    
    if (STATE.addStudentStep === 4) {
        nextBtn.innerHTML = `<span>Submit Enrollment</span> <i class="bi bi-check-circle ms-1"></i>`;
        nextBtn.className = "btn btn-success py-2 px-4";
    } else {
        nextBtn.innerHTML = `<span>Next</span> <i class="bi bi-chevron-right ms-1" id="btn-add-step-icon"></i>`;
        nextBtn.className = "btn btn-diagnostic py-2 px-4";
    }

    // Progress bar fill calculation
    const progressWidth = ((STATE.addStudentStep - 1) / 3) * 100;
    progressBar.style.width = `${progressWidth}%`;
}

function validateAddStep(step) {
    let isValid = true;
    clearFormErrors();

    if (step === 1) {
        const fname = document.getElementById('add-first-name');
        const lname = document.getElementById('add-last-name');
        const dob = document.getElementById('add-dob');

        if (!fname.value.trim()) {
            showInputError('add-first-name', 'First name is required');
            isValid = false;
        }
        if (!lname.value.trim()) {
            showInputError('add-last-name', 'Last name is required');
            isValid = false;
        }
        if (!dob.value) {
            showInputError('add-dob', 'Date of birth is required');
            isValid = false;
        }
    } else if (step === 2) {
        const email = document.getElementById('add-email');
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!email.value.trim() || !emailRegex.test(email.value)) {
            showInputError('add-email', 'Provide a valid email address');
            isValid = false;
        }
    } else if (step === 3) {
        const code = document.getElementById('add-student-number');
        const dept = document.getElementById('add-department');
        const codeRegex = /^STD-\d{4}-\d{4}$/;

        if (!code.value.trim() || !codeRegex.test(code.value)) {
            showInputError('add-student-number', 'Registration code must match pattern STD-YYYY-NNNN');
            isValid = false;
        }
        if (!dept.value) {
            showInputError('add-department', 'Select a department faculty');
            isValid = false;
        }
    }
    return isValid;
}

function submitAddStudentForm() {
    const payload = {
        studentNumber: document.getElementById('add-student-number').value.trim(),
        firstName: document.getElementById('add-first-name').value.trim(),
        lastName: document.getElementById('add-last-name').value.trim(),
        email: document.getElementById('add-email').value.trim(),
        phone: document.getElementById('add-phone').value.trim() || null,
        dateOfBirth: document.getElementById('add-dob').value,
        departmentId: parseInt(document.getElementById('add-department').value)
    };

    authorizedFetch('/api/students', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => {
        if (!res.ok) {
            return res.json().then(data => { throw data; });
        }
        return res.json();
    })
    .then(student => {
        // If image selected, upload the photo (Sprint 15)
        const photoFile = document.getElementById('add-photo-file').files[0];
        if (photoFile) {
            uploadProfilePhoto(student.id, photoFile, () => {
                showToast("Registration and photo upload completed!", "success");
                finishStudentAddSuccess();
            });
        } else {
            showToast("Student profile registered successfully.", "success");
            finishStudentAddSuccess();
        }
    })
    .catch(err => {
        // Map backend validation errors (Sprint 5 validation handler)
        if (err.details && typeof err.details === 'object') {
            Object.keys(err.details).forEach(key => {
                showToast(`Validation error: ${err.details[key]}`, "error");
            });
        } else {
            showToast(err.message || "Failed to register student record.", "error");
        }
    });
}

function uploadProfilePhoto(studentId, file, callback) {
    const formData = new FormData();
    formData.append("photo", file);

    authorizedFetch(`/api/students/${studentId}/photo`, {
        method: 'POST',
        body: formData
    })
    .then(res => {
        if (!res.ok) throw new Error("Image upload failed.");
        return res.json();
    })
    .then(() => {
        if (callback) callback();
    })
    .catch(err => {
        showToast("Photo upload failed. Standard credentials saved.", "warning");
        if (callback) callback();
    });
}

function finishStudentAddSuccess() {
    // Reset Add Form fields
    document.getElementById('add-student-form').reset();
    document.getElementById('add-avatar-preview').src = 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=150';
    document.getElementById('add-file-name-label').textContent = 'No file selected (Supports JPG, PNG < 2MB)';
    
    STATE.addStudentStep = 1;
    updateAddStepUI();

    // Switch view back to list directory
    switchTab('students-list');
}

/* =========================================================================
   Edit Student View (Sprint 9)
   ========================================================================= */
function showEditStudentForm(id) {
    authorizedFetch(`/api/students/${id}`)
        .then(res => res.json())
        .then(student => {
            // Pre-fill Edit inputs
            document.getElementById('edit-student-id').value = student.id;
            document.getElementById('edit-first-name').value = student.firstName;
            document.getElementById('edit-last-name').value = student.lastName;
            document.getElementById('edit-student-number').value = student.studentNumber;
            document.getElementById('edit-dob').value = student.dateOfBirth;
            document.getElementById('edit-email').value = student.email;
            document.getElementById('edit-phone').value = student.phone || '';
            document.getElementById('edit-department').value = student.departmentId;

            // Load photo preview
            const avatar = student.photoPath || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=100';
            document.getElementById('edit-avatar-preview').src = avatar;

            // Setup photo upload triggers
            const photoInput = document.getElementById('edit-photo-file');
            photoInput.onchange = () => {
                const file = photoInput.files[0];
                if (file) {
                    const reader = new FileReader();
                    reader.onload = (e) => document.getElementById('edit-avatar-preview').src = e.target.result;
                    reader.readAsDataURL(file);
                }
            };

            // Setup cancel handlers
            const cancelEdit = () => {
                if (confirm("Cancel edits? All unsaved updates will be discarded.")) {
                    switchTab('students-list');
                }
            };
            document.getElementById('btn-edit-back').onclick = cancelEdit;
            document.getElementById('btn-edit-cancel').onclick = cancelEdit;

            // Bind submit
            document.getElementById('edit-student-form').onsubmit = (e) => {
                e.preventDefault();
                submitEditStudentForm(student.id);
            };

            // Switch to edit section
            switchTab('edit-student');
        })
        .catch(err => showToast("Failed to fetch student record details.", "error"));
}

function submitEditStudentForm(id) {
    const payload = {
        studentNumber: document.getElementById('edit-student-number').value.trim(),
        firstName: document.getElementById('edit-first-name').value.trim(),
        lastName: document.getElementById('edit-last-name').value.trim(),
        email: document.getElementById('edit-email').value.trim(),
        phone: document.getElementById('edit-phone').value.trim() || null,
        dateOfBirth: document.getElementById('edit-dob').value,
        departmentId: parseInt(document.getElementById('edit-department').value)
    };

    const spinner = document.getElementById('edit-save-spinner');
    if (spinner) spinner.classList.remove('d-none');

    authorizedFetch(`/api/students/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => {
        if (!res.ok) return res.json().then(data => { throw data; });
        return res.json();
    })
    .then(student => {
        // Upload photo if file selected
        const photoFile = document.getElementById('edit-photo-file').files[0];
        if (photoFile) {
            uploadProfilePhoto(student.id, photoFile, () => {
                showToast("Student profile details and photo updated successfully.", "success");
                switchTab('students-list');
            });
        } else {
            showToast("Student profile details updated successfully.", "success");
            switchTab('students-list');
        }
    })
    .catch(err => {
        if (err.details && typeof err.details === 'object') {
            Object.keys(err.details).forEach(key => {
                showToast(`Validation error: ${err.details[key]}`, "error");
            });
        } else {
            showToast(err.message || "Failed to update profile.", "error");
        }
    })
    .finally(() => {
        if (spinner) spinner.classList.add('d-none');
    });
}

/* =========================================================================
   Student Details Page & Course Enrollments Timeline (Sprint 10)
   ========================================================================= */
function showStudentDetails(id) {
    authorizedFetch(`/api/students/${id}`)
        .then(res => res.json())
        .then(student => {
            const avatar = student.photoPath || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=150';
            
            // Populating profile elements
            document.getElementById('details-avatar').src = avatar;
            document.getElementById('details-name').textContent = `${student.firstName} ${student.lastName}`;
            document.getElementById('details-badge-dept').textContent = student.departmentName || 'N/A';
            document.getElementById('details-id-code').textContent = student.studentNumber;
            document.getElementById('details-joined-date').textContent = new Date(student.createdAt).toLocaleDateString();
            document.getElementById('details-updated-date').textContent = new Date(student.updatedAt).toLocaleTimeString();
            
            const isSoftDeleted = student.deletedAt !== null;
            document.getElementById('details-status-badge').innerHTML = isSoftDeleted ? 
                `<span class="badge-custom red"><i class="bi bi-x-circle-fill me-1"></i>Deleted</span>` : 
                `<span class="badge-custom green"><i class="bi bi-check-circle-fill me-1"></i>Active</span>`;

            // Contact mapping
            document.getElementById('details-email').textContent = student.email;
            document.getElementById('details-phone').textContent = student.phone || 'N/A';
            document.getElementById('details-dob').textContent = new Date(student.dateOfBirth).toLocaleDateString();
            document.getElementById('details-dept-name').textContent = student.departmentName || 'N/A';

            // Load interactive timelines mapping active courses based on department ID
            loadDetailsTimeline(student.departmentCode);

            // Bind actions buttons
            document.getElementById('btn-details-back').onclick = () => switchTab('students-list');
            const editDetailsBtn = document.getElementById('btn-details-edit');
            if (editDetailsBtn) {
                if (STATE.directory.adminAccess) {
                    editDetailsBtn.classList.remove('d-none');
                    editDetailsBtn.onclick = () => showEditStudentForm(student.id);
                } else {
                    editDetailsBtn.classList.add('d-none');
                }
            }

            switchTab('student-details');
        })
        .catch(err => showToast("Could not load student profile sheet.", "error"));
}

function loadDetailsTimeline(deptCode) {
    const timeline = document.getElementById('details-enrollments-timeline');
    timeline.innerHTML = '';

    // Mock dynamic syllabus records matching database seed structure
    const coursesSyllabus = {
        'CS': [
            { code: 'CS101', title: 'Introduction to Java Programming', credits: 4, grade: 'A', date: 'Jan 2026', state: 'COMPLETED' },
            { code: 'CS201', title: 'Database Management Systems', credits: 3, grade: 'Pending', date: 'Jul 2026', state: 'ENROLLED' }
        ],
        'ME': [
            { code: 'ME101', title: 'Applied Thermodynamics', credits: 4, grade: 'B+', date: 'Jan 2026', state: 'COMPLETED' }
        ],
        'BA': [
            { code: 'BA101', title: 'Principles of Financial Accounting', credits: 3, grade: 'Pending', date: 'Jul 2026', state: 'ENROLLED' }
        ]
    };

    const studentCourses = coursesSyllabus[deptCode] || [];
    
    if (studentCourses.length === 0) {
        timeline.innerHTML = '<span class="text-muted small">No course enrollments found.</span>';
        return;
    }

    studentCourses.forEach(course => {
        const item = document.createElement('div');
        item.className = `timeline-item ${course.state.toLowerCase()}`;
        
        let gradeBadge = course.grade === 'Pending' ? 
            `<span class="badge-custom amber py-0 px-2 ms-2 small">In Progress</span>` :
            `<span class="badge-custom green py-0 px-2 ms-2 small">Grade: ${course.grade}</span>`;

        item.innerHTML = `
            <div class="timeline-dot"></div>
            <div class="timeline-content">
                <div class="d-flex justify-content-between align-items-center mb-1">
                    <span class="timeline-title">${course.code} - ${course.title}</span>
                    ${gradeBadge}
                </div>
                <div class="timeline-meta">Credits: ${course.credits} &bull; Semester: ${course.date}</div>
            </div>
        `;
        timeline.appendChild(item);
    });
}

/* =========================================================================
   Diagnostics Console
   ========================================================================= */
function initDiagnostics() {
    runDiagnosticsQuery();
    const triggerBtn = document.getElementById('btn-run-diagnostics');
    const navBtn = document.getElementById('nav-diagnostic-btn');
    const dashRefresh = document.getElementById('btn-dashboard-refresh');

    if (triggerBtn) triggerBtn.addEventListener('click', () => runDiagnosticsQuery(true));
    if (navBtn) navBtn.addEventListener('click', () => switchTab('diagnostics'));
    if (dashRefresh) dashRefresh.addEventListener('click', () => {
        loadDashboardData();
        showToast("Dashboard stats refreshed.", "success");
    });
}

function runDiagnosticsQuery(manualTrigger = false) {
    const dbStatusText = document.getElementById('db-status-text');
    const dbDetailsText = document.getElementById('db-details-text');
    const jvmVersionText = document.getElementById('jvm-version-text');
    const uptimeText = document.getElementById('uptime-text');
    const lastCheckText = document.getElementById('last-check-time');
    const statusDot = document.getElementById('nav-status-dot');
    const statusText = document.querySelector('#nav-diagnostic-btn span:last-child');
    const loader = document.getElementById('diagnostics-loader');

    if (manualTrigger && loader) loader.classList.remove('d-none');

    // Diagnostics bypasses standard JWT filters (public check)
    fetch('/api/health')
        .then(res => {
            if (!res.ok) throw new Error("Offline");
            return res.json();
        })
        .then(data => {
            setTimeout(() => {
                if (loader) loader.classList.add('d-none');
                
                if (dbStatusText) {
                    dbStatusText.textContent = data.databaseStatus;
                    dbStatusText.className = 'text-success fw-bold';
                    dbDetailsText.textContent = data.databaseDetails;
                    jvmVersionText.textContent = `Java ${data.jvmVersion}`;
                    uptimeText.textContent = formatUptimeString(data.uptimeMs);
                    lastCheckText.textContent = new Date(data.timestamp).toLocaleTimeString();
                }

                if (statusDot) statusDot.className = 'status-indicator up pulse';
                if (statusText) statusText.textContent = 'Sys Online';

                if (manualTrigger) showToast("Diagnostic check completed successfully.", "success");
            }, manualTrigger ? 500 : 0);
        })
        .catch(() => {
            if (loader) loader.classList.add('d-none');
            
            if (dbStatusText) {
                dbStatusText.textContent = 'OFFLINE';
                dbStatusText.className = 'text-danger fw-bold';
                dbDetailsText.textContent = 'DB Context unreachable.';
                jvmVersionText.textContent = 'N/A';
                uptimeText.textContent = '0s';
                lastCheckText.textContent = new Date().toLocaleTimeString();
            }

            if (statusDot) statusDot.className = 'status-indicator down';
            if (statusText) statusText.textContent = 'Sys Alert';
        });
}

function formatUptimeString(ms) {
    if (ms <= 0 || isNaN(ms)) return '0s';
    let seconds = Math.floor(ms / 1000);
    let minutes = Math.floor(seconds / 60);
    let hours = Math.floor(minutes / 60);

    seconds = seconds % 60;
    minutes = minutes % 60;

    let result = '';
    if (hours > 0) result += `${hours}h `;
    if (minutes > 0 || hours > 0) result += `${minutes}m `;
    result += `${seconds}s`;
    return result;
}

/* =========================================================================
   Utility Methods (Toasts, Debounces, Form Errors)
   ========================================================================= */
function showToast(message, type = "success") {
    const toastEl = document.getElementById('sms-live-toast');
    const toastIcon = document.getElementById('toast-icon');
    const toastText = document.getElementById('toast-message-text');

    if (!toastEl) return;

    // Apply colors
    toastEl.classList.remove('bg-success', 'bg-danger', 'bg-warning');
    if (type === "success") {
        toastEl.classList.add('bg-success');
        toastIcon.className = "bi bi-check-circle-fill text-white fs-6";
    } else if (type === "error") {
        toastEl.classList.add('bg-danger');
        toastIcon.className = "bi bi-exclamation-triangle-fill text-white fs-6";
    } else if (type === "warning") {
        toastEl.classList.add('bg-warning');
        toastIcon.className = "bi bi-info-circle-fill text-white fs-6";
    }

    toastText.textContent = message;
    
    // Show using bootstrap
    const bootstrapToast = new bootstrap.Toast(toastEl, { delay: 3500 });
    bootstrapToast.show();
}

function debounce(func, delay) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), delay);
    };
}

function showInputError(id, msg) {
    const input = document.getElementById(id);
    const errorDiv = document.getElementById(`err-${id}`);
    if (input) input.classList.add('is-invalid');
    if (errorDiv) {
        errorDiv.textContent = msg;
        errorDiv.style.display = 'block';
    }
}

function clearFormErrors() {
    document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    document.querySelectorAll('.invalid-feedback').forEach(el => {
        el.textContent = '';
        el.style.display = 'none';
    });
}

function logError(msg, err) {
    console.error(`[SMS Error] ${msg}:`, err);
}

/* =========================================================================
   Interactive Dashboard Stats Cards Popup details
   ========================================================================= */
function initStatsCardsClickEvents() {
    const studentsCard = document.getElementById('card-stats-students');
    const deptsCard = document.getElementById('card-stats-faculties');
    const coursesCard = document.getElementById('card-stats-courses');
    const modal = document.getElementById('info-modal-overlay');
    const closeBtn = document.getElementById('btn-close-info-modal');

    if (closeBtn && modal) {
        closeBtn.addEventListener('click', () => {
            modal.classList.add('d-none');
        });
        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.classList.add('d-none');
        });
    }

    if (studentsCard) {
        studentsCard.addEventListener('click', () => {
            switchTab('students-list');
        });
    }

    if (deptsCard) {
        deptsCard.addEventListener('click', () => {
            showFacultiesModal();
        });
    }

    if (coursesCard) {
        coursesCard.addEventListener('click', () => {
            showCoursesModal();
        });
    }
}

function showFacultiesModal() {
    const title = document.getElementById('info-modal-title');
    const body = document.getElementById('info-modal-body-content');
    const modal = document.getElementById('info-modal-overlay');
    
    title.innerHTML = `<i class="bi bi-layers-fill text-purple me-2"></i>Faculty Registry`;
    
    // Count active students per department from local state
    const deptCounts = { 1: 0, 2: 0, 3: 0 };
    if (STATE.students && Array.isArray(STATE.students)) {
        STATE.students.forEach(s => {
            if (s.departmentId && deptCounts[s.departmentId] !== undefined && s.deletedAt === null) {
                deptCounts[s.departmentId]++;
            }
        });
    }

    const faculties = [
        { id: 1, code: 'CS', name: 'Computer Science & Engineering', dean: 'Dr. Alan Turing', room: 'Block A - 401', students: deptCounts[1] },
        { id: 2, code: 'ME', name: 'Mechanical Engineering', dean: 'Dr. Nikola Tesla', room: 'Block B - 105', students: deptCounts[2] },
        { id: 3, code: 'BA', name: 'Business Administration', dean: 'Dr. Adam Smith', room: 'Block C - 202', students: deptCounts[3] }
    ];

    let html = `
        <p class="text-secondary small mb-3">Seeded faculty departments registered in database:</p>
        <div class="d-flex flex-column gap-3">
    `;

    faculties.forEach(f => {
        html += `
            <div class="glass-card p-3 border-secondary border-opacity-30 bg-dark bg-opacity-20" style="backdrop-filter: none; box-shadow: none;">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="badge-custom purple fw-bold">${f.code}</span>
                    <span class="text-info small fw-semibold">${f.students} Active Students</span>
                </div>
                <h5 class="text-white fs-6 m-0 mb-1">${f.name}</h5>
                <div class="text-muted small mt-2">
                    <div>Dean: <span class="text-secondary fw-semibold">${f.dean}</span></div>
                    <div>Location: <span class="text-secondary">${f.room}</span></div>
                </div>
            </div>
        `;
    });

    html += `</div>`;
    body.innerHTML = html;
    modal.classList.remove('d-none');
}

function showCoursesModal() {
    const title = document.getElementById('info-modal-title');
    const body = document.getElementById('info-modal-body-content');
    const modal = document.getElementById('info-modal-overlay');
    
    title.innerHTML = `<i class="bi bi-book-half text-teal me-2"></i>Active Syllabus Courses`;
    
    const courses = [
        { code: 'CS101', title: 'Introduction to Java Programming', credits: 4, dept: 'Computer Science & Eng', level: 'Introductory' },
        { code: 'CS201', title: 'Database Management Systems', credits: 3, dept: 'Computer Science & Eng', level: 'Intermediate' },
        { code: 'CS301', title: 'Clean Architecture Patterns', credits: 4, dept: 'Computer Science & Eng', level: 'Advanced' },
        { code: 'ME101', title: 'Applied Thermodynamics', credits: 4, dept: 'Mechanical Engineering', level: 'Introductory' },
        { code: 'ME202', title: 'Fluid Mechanics & Machinery', credits: 3, dept: 'Mechanical Engineering', level: 'Intermediate' },
        { code: 'BA101', title: 'Principles of Financial Accounting', credits: 3, dept: 'Business Administration', level: 'Introductory' },
        { code: 'BA202', title: 'Organizational Behavior & Management', credits: 3, dept: 'Business Administration', level: 'Intermediate' }
    ];

    let html = `
        <p class="text-secondary small mb-3">Available academic course structures mapped to departments:</p>
        <div class="d-flex flex-column gap-2">
    `;

    courses.forEach(c => {
        html += `
            <div class="d-flex justify-content-between align-items-center p-3 border-bottom border-secondary border-opacity-10">
                <div>
                    <div class="d-flex align-items-center gap-2">
                        <code class="text-info fw-bold">${c.code}</code>
                        <span class="text-white fw-semibold small">${c.title}</span>
                    </div>
                    <span class="text-muted small d-block mt-1">${c.dept} &bull; ${c.level}</span>
                </div>
                <span class="badge bg-dark border border-secondary text-secondary small">${c.credits} Credits</span>
            </div>
        `;
    });

    html += `</div>`;
    body.innerHTML = html;
    modal.classList.remove('d-none');
}

function loadFacultyPageData() {
    const container = document.getElementById('faculties-mgmt-container');
    if (!container) return;

    // Count students per department
    const deptStudents = { 1: [], 2: [], 3: [] };
    if (STATE.students && Array.isArray(STATE.students)) {
        STATE.students.forEach(s => {
            if (s.departmentId && deptStudents[s.departmentId] !== undefined && s.deletedAt === null) {
                deptStudents[s.departmentId].push(s);
            }
        });
    }

    const faculties = [
        { id: 1, code: 'CS', name: 'Computer Science & Engineering', dean: 'Dr. Alan Turing', room: 'Block A - 401', students: deptStudents[1] },
        { id: 2, code: 'ME', name: 'Mechanical Engineering', dean: 'Dr. Nikola Tesla', room: 'Block B - 105', students: deptStudents[2] },
        { id: 3, code: 'BA', name: 'Business Administration', dean: 'Dr. Adam Smith', room: 'Block C - 202', students: deptStudents[3] }
    ];

    container.innerHTML = '';

    faculties.forEach(f => {
        const col = document.createElement('div');
        col.className = 'col-lg-4 col-md-12';
        
        let studentsHtml = '';
        if (f.students.length === 0) {
            studentsHtml = '<div class="text-muted small py-3 text-center">No active student enrollments.</div>';
        } else {
            f.students.forEach(s => {
                const avatar = s.photoPath || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=30';
                studentsHtml += `
                    <div class="d-flex align-items-center justify-content-between p-2 border-bottom border-secondary border-opacity-10">
                        <div class="d-flex align-items-center gap-2">
                            <img src="${avatar}" alt="Avatar" class="rounded-circle" style="width: 28px; height: 28px; object-fit: cover;">
                            <div>
                                <span class="text-white small fw-semibold d-block">${s.firstName} ${s.lastName}</span>
                                <code class="text-muted" style="font-size: 0.65rem;">${s.studentNumber}</code>
                            </div>
                        </div>
                        <button class="btn btn-link text-info p-0 btn-sm text-decoration-none" onclick="showStudentDetails(${s.id})">
                            <i class="bi bi-chevron-right"></i>
                        </button>
                    </div>
                `;
            });
        }

        col.innerHTML = `
            <div class="glass-card h-100 p-4">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <span class="badge-custom purple fw-bold">${f.code}</span>
                    <span class="badge bg-dark border border-secondary text-secondary small">${f.students.length} Enrolled</span>
                </div>
                <h4 class="text-white fs-5 m-0 mb-1">${f.name}</h4>
                <div class="text-muted small mt-2 mb-4">
                    <div>Dean: <span class="text-secondary fw-semibold">${f.dean}</span></div>
                    <div>Office: <span class="text-secondary">${f.room}</span></div>
                </div>
                <h5 class="text-white fs-7 border-bottom border-secondary border-opacity-15 pb-2 mb-2">Student Registry</h5>
                <div style="max-height: 250px; overflow-y: auto;">
                    ${studentsHtml}
                </div>
            </div>
        `;
        container.appendChild(col);
    });
}
