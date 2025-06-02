// Dữ liệu mẫu (trong thực tế sẽ được lấy từ API)
const computers = [
    { id: 'PC01', name: 'Máy 01', status: 'available', type: 'LMAY01', price: 10000 },
    { id: 'PC02', name: 'Máy 02', status: 'in-use', type: 'LMAY01', price: 10000 },
    { id: 'PC03', name: 'Máy 03', status: 'maintenance', type: 'LMAY02', price: 15000 },
    // Thêm các máy khác...
];

const services = [
    { id: 'DV004', name: 'Snack Oishi', price: 8000, status: 'available' },
    { id: 'DV005', name: 'Nước lọc Aquafina', price: 7000, status: 'available' },
    { id: 'DV006', name: 'Mì tôm Hảo Hảo', price: 10000, status: 'available' },
    { id: 'DV007', name: 'Coca-Cola lon', price: 12000, status: 'available' },
    { id: 'DV008', name: 'Sting dâu chai', price: 15000, status: 'available' },
];

// Dữ liệu tài khoản mẫu (trong thực tế sẽ được lấy từ database)
const accounts = [
    { username: 'user_an01', password: 'pass001', balance: 150000, name: 'Nguyễn Văn An' },
    { username: 'user_binh02', password: 'pass002', balance: 200000, name: 'Trần Thị Bình' },
    { username: 'user_cuong03', password: 'pass003', balance: 500000, name: 'Lê Văn Cường' },
];

// Biến lưu trữ thông tin người dùng đang đăng nhập
let currentUser = null;
let currentSession = null;
let remainingTimeInterval = null;
let serviceBill = [];

// Hàm format tiền tệ
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Hàm format thời gian
function formatTime(seconds) {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
}

// Hàm tính thời gian có thể sử dụng dựa trên số dư
function calculateAvailableTime(balance, hourlyRate) {
    return Math.floor((balance / hourlyRate) * 3600); // Trả về số giây
}

// Hàm cập nhật thông tin tài khoản
function updateAccountInfo() {
    if (currentUser) {
        document.getElementById('accountBalance').textContent = formatCurrency(currentUser.balance);
        
        if (currentSession) {
            document.getElementById('currentComputer').textContent = currentSession.computerName;
            document.getElementById('hourlyRate').textContent = formatCurrency(currentSession.price);
            updateRemainingTime();
        } else {
            document.getElementById('currentComputer').textContent = 'Chưa sử dụng';
            document.getElementById('remainingTime').textContent = '00:00:00';
            document.getElementById('hourlyRate').textContent = '0 VNĐ';
        }
    } else {
        document.getElementById('accountBalance').textContent = '0 VNĐ';
        document.getElementById('currentComputer').textContent = 'Chưa sử dụng';
        document.getElementById('remainingTime').textContent = '00:00:00';
        document.getElementById('hourlyRate').textContent = '0 VNĐ';
    }
}

// Hàm cập nhật thời gian còn lại
function updateRemainingTime() {
    if (!currentSession || !currentUser) return;
    
    const now = new Date();
    const startTime = new Date(currentSession.startTime);
    const hoursUsed = (now - startTime) / (1000 * 60 * 60);
    const cost = Math.ceil(hoursUsed * currentSession.price);
    
    // Tính thời gian còn lại dựa trên số dư hiện tại
    const remainingBalance = currentUser.balance - cost;
    const remainingSeconds = Math.floor((remainingBalance / currentSession.price) * 3600);
    
    document.getElementById('remainingTime').textContent = formatTime(remainingSeconds);
    
    if (remainingSeconds <= 0) {
        clearInterval(remainingTimeInterval);
        endSession();
    }
}

// Hàm kết thúc phiên sử dụng
function endSession() {
    if (!currentSession) return;

    const computer = computers.find(c => c.id === currentSession.computerId);
    if (computer) {
        computer.status = 'available';
    }

    // Tính tiền sử dụng máy
    const now = new Date();
    const startTime = new Date(currentSession.startTime);
    const hoursUsed = (now - startTime) / (1000 * 60 * 60);
    const cost = Math.ceil(hoursUsed * currentSession.price);
    
    currentUser.balance -= cost;
    
    // Hiển thị hóa đơn
    const billHtml = `
        <div class="bill-item">
            <div class="d-flex justify-content-between">
                <span>Sử dụng máy ${currentSession.computerName}</span>
                <span class="text-danger">-${formatCurrency(cost)}</span>
            </div>
            <small class="text-muted">${formatTime(Math.floor(hoursUsed * 3600))}</small>
        </div>
    `;
    addToBill(billHtml, cost);

    currentSession = null;
    updateAccountInfo();
    displayComputers();
}

// Hàm thêm vào hóa đơn
function addToBill(itemHtml, amount, isRecharge = false) {
    const billContainer = document.getElementById('serviceBill');
    const totalBillElement = document.getElementById('totalBill');
    
    // Xóa thông báo "Chưa có dịch vụ nào được mua" nếu có
    const emptyMessage = billContainer.querySelector('.text-muted');
    if (emptyMessage) {
        emptyMessage.remove();
    }
    
    // Thêm item mới
    const itemDiv = document.createElement('div');
    itemDiv.innerHTML = itemHtml;
    billContainer.appendChild(itemDiv);
    
    // Cập nhật tổng tiền (tính cả nạp tiền và chi tiêu)
    const currentTotal = parseFloat(totalBillElement.textContent.replace(/[^0-9.-]+/g, ''));
    const newTotal = currentTotal - amount; // Luôn trừ đi số tiền (cả nạp và chi)
    totalBillElement.textContent = formatCurrency(newTotal);
}

// Hàm bắt đầu sử dụng máy
function startUsingComputer(computerId) {
    const computer = computers.find(c => c.id === computerId);
    if (!computer || computer.status !== 'available') return;

    // Tính thời gian có thể sử dụng dựa trên số dư
    const availableSeconds = calculateAvailableTime(currentUser.balance, computer.price);
    if (availableSeconds <= 0) {
        alert('Số dư không đủ để sử dụng máy!');
        return;
    }

    // Giả lập việc bắt đầu phiên sử dụng
    const startTime = new Date();
    const endTime = new Date(startTime.getTime() + availableSeconds * 1000);

    currentSession = {
        computerId,
        computerName: computer.name,
        startTime,
        endTime,
        price: computer.price
    };

    computer.status = 'in-use';
    updateAccountInfo();
    displayComputers();

    // Cập nhật thời gian còn lại mỗi giây
    if (remainingTimeInterval) clearInterval(remainingTimeInterval);
    remainingTimeInterval = setInterval(updateRemainingTime, 1000);
}

// Hàm hiển thị danh sách máy tính
function displayComputers() {
    const computerList = document.getElementById('computerList');
    computerList.innerHTML = '';

    computers.forEach(computer => {
        const computerCard = document.createElement('div');
        computerCard.className = `col-md-4 mb-3`;
        computerCard.innerHTML = `
            <div class="computer-card ${computer.status}">
                <h6>${computer.name}</h6>
                <p class="mb-1">Loại: ${computer.type}</p>
                <p class="mb-1">Giá: ${formatCurrency(computer.price)}/giờ</p>
                <p class="mb-0">Trạng thái: ${getStatusText(computer.status)}</p>
                ${computer.status === 'available' ? `
                    <button class="btn btn-primary btn-sm mt-2 use-computer-btn" data-computer-id="${computer.id}">
                        Sử dụng
                    </button>
                ` : ''}
            </div>
        `;
        computerList.appendChild(computerCard);
    });

    // Thêm sự kiện cho các nút sử dụng máy
    document.querySelectorAll('.use-computer-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            if (!currentUser) {
                showLoginModal();
                return;
            }
            const computerId = btn.dataset.computerId;
            startUsingComputer(computerId);
        });
    });
}

// Hàm hiển thị danh sách dịch vụ
function displayServices() {
    const serviceList = document.getElementById('serviceList');
    serviceList.innerHTML = '';

    services.forEach(service => {
        const serviceItem = document.createElement('div');
        serviceItem.className = 'service-item';
        serviceItem.innerHTML = `
            <div class="d-flex justify-content-between align-items-center">
                <span class="service-name">${service.name}</span>
                <div>
                    <span class="service-price me-2">${formatCurrency(service.price)}</span>
                    <button class="btn btn-sm btn-outline-primary buy-service-btn" data-service-id="${service.id}">
                        Mua
                    </button>
                </div>
            </div>
        `;
        serviceList.appendChild(serviceItem);
    });

    // Thêm sự kiện cho các nút mua dịch vụ
    document.querySelectorAll('.buy-service-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            if (!currentUser) {
                showLoginModal();
                return;
            }
            const serviceId = btn.dataset.serviceId;
            buyService(serviceId);
        });
    });
}

// Hàm mua dịch vụ
function buyService(serviceId) {
    const service = services.find(s => s.id === serviceId);
    if (!service || service.status !== 'available') return;

    if (currentUser.balance < service.price) {
        alert('Số dư không đủ để mua dịch vụ này!');
        return;
    }

    currentUser.balance -= service.price;
    
    // Thêm vào hóa đơn
    const billHtml = `
        <div class="bill-item">
            <div class="d-flex justify-content-between">
                <span>${service.name}</span>
                <span class="text-danger">-${formatCurrency(service.price)}</span>
            </div>
        </div>
    `;
    addToBill(billHtml, service.price);
    
    updateAccountInfo();
    alert(`Mua ${service.name} thành công!`);
}

// Hàm chuyển đổi trạng thái sang text
function getStatusText(status) {
    switch(status) {
        case 'available':
            return 'Khả dụng';
        case 'in-use':
            return 'Đang sử dụng';
        case 'maintenance':
            return 'Bảo trì';
        default:
            return 'Không xác định';
    }
}

// Hàm hiển thị modal đăng nhập
function showLoginModal() {
    const loginModal = new bootstrap.Modal(document.getElementById('loginModal'));
    loginModal.show();
}

// Hàm hiển thị modal nạp tiền
function showRechargeModal(amount) {
    const rechargeModal = new bootstrap.Modal(document.getElementById('rechargeModal'));
    document.getElementById('currentBalance').textContent = formatCurrency(currentUser.balance);
    document.getElementById('rechargeSuccess').classList.add('d-none');
    rechargeModal.show();
}

// Hàm xử lý đăng nhập
function handleLogin(username, password) {
    const user = accounts.find(acc => acc.username === username && acc.password === password);
    if (user) {
        currentUser = user;
        document.getElementById('loginBtn').classList.add('d-none');
        document.getElementById('userInfo').classList.remove('d-none');
        document.getElementById('userName').textContent = user.name;
        updateAccountInfo();
        return true;
    }
    return false;
}

// Hàm xử lý đăng xuất
function handleLogout() {
    currentUser = null;
    currentSession = null;
    if (remainingTimeInterval) clearInterval(remainingTimeInterval);
    document.getElementById('loginBtn').classList.remove('d-none');
    document.getElementById('userInfo').classList.add('d-none');
    updateAccountInfo();
}

// Hàm xử lý nạp tiền
function handleRecharge(amount) {
    if (currentUser) {
        currentUser.balance += amount;
        document.getElementById('currentBalance').textContent = formatCurrency(currentUser.balance);
        const successAlert = document.getElementById('rechargeSuccess');
        successAlert.textContent = `Nạp tiền thành công: ${formatCurrency(amount)}`;
        successAlert.classList.remove('d-none');
        
        // Thêm vào hóa đơn với isRecharge = true
        const billHtml = `
            <div class="bill-item">
                <div class="d-flex justify-content-between">
                    <span>Nạp tiền</span>
                    <span class="text-danger">-${formatCurrency(amount)}</span>
                </div>
            </div>
        `;
        addToBill(billHtml, amount, true);
        
        updateAccountInfo();
    }
}

// Khởi tạo trang
function init() {
    updateAccountInfo();
    displayComputers();
    displayServices();

    // Xử lý sự kiện đăng nhập
    document.getElementById('loginBtn').addEventListener('click', showLoginModal);
    document.getElementById('submitLogin').addEventListener('click', () => {
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        const loginError = document.getElementById('loginError');

        if (handleLogin(username, password)) {
            bootstrap.Modal.getInstance(document.getElementById('loginModal')).hide();
            loginError.classList.add('d-none');
            // Reset hóa đơn khi đăng nhập mới
            document.getElementById('serviceBill').innerHTML = '<p class="text-muted">Chưa có dịch vụ nào được mua</p>';
            document.getElementById('totalBill').textContent = '0 VNĐ';
        } else {
            loginError.textContent = 'Tên đăng nhập hoặc mật khẩu không đúng';
            loginError.classList.remove('d-none');
        }
    });

    // Xử lý sự kiện đăng xuất
    document.getElementById('logoutBtn').addEventListener('click', () => {
        if (currentSession) {
            endSession();
        }
        handleLogout();
    });

    // Xử lý sự kiện nạp tiền
    document.querySelectorAll('.recharge-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            if (!currentUser) {
                showLoginModal();
                return;
            }
            const amount = parseInt(btn.dataset.amount);
            handleRecharge(amount);
        });
    });
}

// Chạy khi trang đã load xong
document.addEventListener('DOMContentLoaded', init); 