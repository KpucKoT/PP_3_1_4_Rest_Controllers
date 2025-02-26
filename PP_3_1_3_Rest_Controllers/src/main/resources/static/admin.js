document.addEventListener('DOMContentLoaded', function () {

    let initialPage = sessionStorage.getItem('lastPage') || 'users';

    if (!history.state) {
        console.log("Инициализируем history.replaceState с:", initialPage);
        history.replaceState({ page: initialPage }, '', window.location.pathname);
    }

    sessionStorage.setItem('lastPage', initialPage);

    console.log("Текущая страница из sessionStorage:", sessionStorage.getItem('lastPage'));

    // Загружаем остальные компоненты страницы
    loadHeader();
    loadSidebar();
    loadUsers();  // Загружаем пользователей
    setupNavigationLinks();
});

//Для перехода на страницу создания пользователя
function setupNavigationLinks() {
    const createUserLink = document.querySelector('[data-bs-target="#create"]'); // Ищем ссылку с data-bs-target="#create"
    if (createUserLink) {
        createUserLink.addEventListener('click', function (event) {
            event.preventDefault();  // Отменяем стандартное поведение ссылки
            console.log("Переход на страницу создания пользователя...");
            setupCreateUserForm();  // Загружаем страницу создания пользователя и настраиваем форму
            history.pushState({ page: 'create-user' }, 'Создать пользователя', '/create-user');  // Меняем URL
            console.log("Состояние после pushState:", history.state);
        });
    }
}

// Функция для создания пользователя
function setupCreateUserForm() {
    const createUserForm = document.getElementById('createUserForm');
    if (createUserForm && !createUserForm.dataset.listenerAdded) { // <=== Проверка, есть ли обработчик
        console.log("Форма найдена, добавляем обработчик submit!");

        createUserForm.addEventListener('submit', function (e) {
            e.preventDefault(); // Предотвращаем перезагрузку страницы
            console.log("Форма отправлена!");

            const username = document.getElementById('createUsername').value;
            const age = document.getElementById('createAge').value;
            const password = document.getElementById('createPassword').value;
            const roles = document.getElementById('createRoles').value;

            const user = {
                username: username,
                age: age,
                password: password,
            };

            console.log("Сейчас вызовем fetch()...");
            console.log("Отправляем запрос на:", '/api/admin/users?roles=' + roles);

            fetch('/api/admin/users?roles=' + roles, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(user)
            })
                .then(response => {
                    console.log("Ответ сервера:", response);
                    if (!response.ok) {
                        alert('Не удалось создать пользователя');
                        return Promise.reject('Ошибка сервера');
                    }
                    return response.json();
                })
                .then(data => {
                    console.log("Ответ JSON:", data);
                    loadUsersPage(); // Обновление страницы с пользователями
                })
                .catch(error => alert('Ошибка создания: ' + error.message));
        });

        createUserForm.dataset.listenerAdded = "true"; // Помечаем, что обработчик уже добавлен
    }
}

//Для возращения назад/вперед
window.onpopstate = function (event) {
    console.log("onpopstate сработал. event.state:", event.state);

    let page = event.state ? event.state.page : sessionStorage.getItem('lastPage');

    if (!page) {
        console.warn("event.state и sessionStorage пустые, устанавливаем значение по умолчанию.");
        page = 'users';
    }

    sessionStorage.setItem('lastPage', page);
    console.log("Загружаем страницу:", page);

    switch (page) {
        case 'user':
            loadUserPage();
            break;
        case 'create-user':
            loadCreateUserPage();  // Убедитесь, что вызывается правильная функция
            break;
        case 'users':
            loadUsersPage();
            break;
        default:
            loadUsersPage();
    }
};

//для сохранения истории
function pushStateWithSession(page, title, url) {
    console.log(`Добавляем ${page} в историю`);

    history.pushState({ page: page }, title, url);
    sessionStorage.setItem('lastPage', page);
    console.log("Текущее состояние sessionStorage:", sessionStorage.getItem('lastPage'));
}

//для шапки
function loadHeader() {
    fetch('/header.html')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Ошибка загрузки header.html: ${response.status}`);
            }
            return response.text();
        })
        .then(html => {
            document.getElementById('header').innerHTML = html;

            // Добавляем обработчик для кнопки "Выйти"
            const logoutButton = document.getElementById('logoutButton');
            if (logoutButton) {
                logoutButton.addEventListener('click', function (event) {
                    event.preventDefault();
                    document.getElementById('logoutForm').submit();
                });
            }

            // Загружаем информацию о пользователе (администраторе)
            loadAdminInfo();
        })
        .catch(error => console.error('Ошибка загрузки верхней панели:', error));
}

// для левой боковой панели
let sidebarLoaded = false;

function loadSidebar() {
    if (sidebarLoaded) return;  // Если боковая панель уже загружена, не загружать заново
    fetch('/sidebar.html')
        .then(response => {
            if (!response.ok) throw new Error(`Ошибка загрузки sidebar.html: ${response.status}`);
            return response.text();
        })
        .then(html => {
            document.getElementById('sidebar').innerHTML = html;
            sidebarLoaded = true;
            addSidebarEventListeners();
        })
        .catch(error => console.error('Ошибка загрузки боковой панели:', error));
}

function addSidebarEventListeners() {
    // Обработчик для "Пользователи"
    const usersLink = document.getElementById('backToHome');
    if (usersLink) {
        usersLink.addEventListener('click', function (event) {
            event.preventDefault();
            loadUsersPage();
        });
    }

    // Обработчик для "Текущий пользователь"
    const currentUserLink = document.getElementById('currentUser');
    if (currentUserLink) {
        currentUserLink.addEventListener('click', function (event) {
            event.preventDefault();
            loadUserPage();
        });
    }

}

// Функция для загрузки страницы пользователей
function loadUsersPage() {
    document.getElementById('sidebar').style.display = 'block';
    fetch('/users.html')
        .then(response => response.text())
        .then(html => {
            const content = document.getElementById('content');

            // Сохраняем структуру, не перезаписываем содержимое боковой панели
            content.innerHTML = '';  // Очищаем содержимое контейнера только для контента

            // Создаем временный div, чтобы вставить HTML
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = html;

            // Убираем дублирующийся col-md-9 из загруженного контента
            const innerContent = tempDiv.querySelector('.col-md-9');
            if (innerContent) {
                content.append(...innerContent.children);  // Вставляем только нужные элементы
            } else {
                content.innerHTML = html;  // Если нет col-md-9, вставляем весь контент
            }

            loadUsers();  // Загружаем данные пользователей
            pushStateWithSession('users', 'Пользователи', '/users.html');
            console.log("Состояние после pushState:", history.state);
            // history.pushState({ page: 'users' }, 'Пользователи', '/users.html');
            setActiveMenuItem('backToHome');
        })
        .catch(error => console.error('Ошибка загрузки пользователей:', error));
}

//чтобы красиво активно подсвечивались кнопки на боковой панели
function setActiveMenuItem(menuItemId) {
    // Снимаем класс active со всех пунктов меню
    const menuItems = document.querySelectorAll('#sidebar .list-group-item');
    menuItems.forEach(item => item.classList.remove('active'));

    // Добавляем класс active к нужному пункту
    const activeItem = document.getElementById(menuItemId);
    if (activeItem) {
        activeItem.classList.add('active');
    }
}

// Загружаем пользователей
function loadUsers() {
    fetch('/api/admin/users')
        .then(response => response.json())

        .then(users => {
            const tableBody = document.getElementById('usersTableBody');
            if (tableBody) {
                tableBody.innerHTML = ''; // Очистить таблицу перед добавлением данных
            } else {
                console.error('Элемент не найден!');
            }

            users.forEach(user => {
                const roles = user.roles.map(role => role.name).join(", ");
                const row = `
                    <tr>
                        <td>${user.id}</td>
                        <td>${user.username}</td>
                        <td>${user.age}</td>
                        <td>${roles}</td>
                        <td><button class="btn btn-primary" onclick="showEditModal(${user.id}, 
                        '${user.username}', ${user.age}, '${roles}')">Редактировать</button></td>
                        <td><button class="btn btn-danger" onclick="fillDeleteModal(${user.id}, 
                        '${user.username}', ${user.age}, '${roles}'); showDeleteModal();" data-bs-toggle="modal" data-bs-target="#deleteUserModal">
                                Удалить
                            </button></td>
                    </tr>
                `;
                tableBody.insertAdjacentHTML('beforeend', row);
            });
        })
        .catch(error => console.error('Ошибка загрузки пользователей:', error));
}



// Загружаем информацию о текущем пользователе (администраторе)
function loadAdminInfo() {
    fetch('api/admin/info')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Ошибка: ${response.status} ${response.statusText}`);
            }
            return response.text();
        })
        .then(data => {
            document.getElementById('adminInfo').innerText = data;
        })
        .catch(error => console.error('Ошибка загрузки информации:', error));
}

// Пример для загрузки страницы пользователя
function loadUserPage() {
    fetch('/user.html')
        .then(response => response.text())
        .then(html => {
            const content = document.getElementById('content');

            // Очищаем контейнер, но не боковую панель
            content.innerHTML = '';

            // Вставляем новый контент
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = html;

            const innerContent = tempDiv.querySelector('.col-md-9');
            if (innerContent) {
                content.append(...innerContent.children);
            } else {
                content.innerHTML = html;
            }

            loadUserInfo();
            history.pushState({ page: 'user' }, 'Пользователь', '/user.html');
            console.log("Состояние после pushState:", history.state);
            setActiveMenuItem('currentUser');
        })
        .catch(error => console.error('Ошибка загрузки пользователя:', error));
}

// Функция для загрузки информации о пользователе с сервера
function loadUserInfo() {
    fetch('/api/admin/me')  // Путь к API для получения данных о текущем пользователе
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при получении данных. Статус: ' + response.status);
            }
            return response.json();
        })
        .then(user => {

            const roles = user.roles ? user.roles : "Нет ролей";
            // Заполняем страницу данными пользователя
            document.getElementById('userTableBody').innerHTML = `
                   <tr>
                    <td>${user.id}</td>
                    <td>${user.username}</td>
                    <td>${user.age}</td>
                    <td>${roles}</td>
                   </tr>
                    `;
        })
        .catch(error => {
            console.error('Ошибка загрузки информации о пользователе:', error);
        });
}

const currentUserLink = document.getElementById('currentUser');
if (currentUserLink) {
    currentUserLink.addEventListener('click', function (event) {
        event.preventDefault();
        loadUserPage();
    });
} else {
    console.error("Элемент #currentUser не найден!");
}

const backToHomeButton = document.getElementById('backToHome');
if (backToHomeButton) {
    backToHomeButton.addEventListener('click', function (event) {
        event.preventDefault();
        loadUsersPage();
        // Добавляем новое состояние в историю
        // history.pushState({ page: 'users' }, 'Пользователи', '/users.html');
        pushStateWithSession('users', 'Пользователи', '/users.html');
        console.log("Состояние после pushState:", history.state);
    });
} else {
    console.error('Элемент #backToHome не найден!');
}


// Открыть модальное окно редактирования
function showEditModal(id, username, age, roles) {
    document.getElementById('editUserId').value = id;
    document.getElementById('editUsername').value = username;
    document.getElementById('editAge').value = age;

    let modal = new bootstrap.Modal(document.getElementById('editUserModal'));
    modal.show();
}

// Обновить пользователя
function updateUser() {
    const id = document.getElementById('editUserId').value;
    const username = document.getElementById('editUsername').value;
    const password = document.getElementById('editPassword').value;
    const age = document.getElementById('editAge').value;
    const roles = document.getElementById('editRoles').value;

    const user = {
        username: username,
        age: age,
        password: password
    };

    fetch(`/api/admin/users/${id}?roles=` + roles, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(user)
    })
        .then(response => {
            if (response.ok) {
                loadUsers(); // Перезагружаем таблицу после обновления
                const modal = bootstrap.Modal.getInstance(document.getElementById('editUserModal'));
                modal.hide(); // Закрываем модальное окно
            } else {
                throw new Error('Не удалось обновить пользователя');
            }
        })
        .catch(error => alert('Ошибка обновления: ' + error.message));
}

// Функция для открытия модального окна
function showDeleteModal() {
    const modal = new bootstrap.Modal(document.getElementById('deleteUserModal'));
    modal.show();
}

// Удалить пользователя
function deleteUser() {
    const userId = document.getElementById('deleteUserId').value;

    fetch(`/api/admin/users/${userId}`, {
        method: 'DELETE'
    })

        .then(response => {
            if (response.ok) {
                loadUsers(); // Перезагружаем таблицу после удаления
                const modal = bootstrap.Modal.getInstance(document.getElementById('deleteUserModal'));
                modal.hide(); // Закрываем модальное окно
                alert('Пользователь успешно удален!');
            } else {
                throw new Error('Не удалось удалить пользователя');
            }
        })
        .catch(error => alert('Ошибка удаления: ' + error.message));
}

// Функция для заполнения модального окна
function fillDeleteModal(userId, username, age, roles) {
    document.getElementById('viewUserId').value = userId;
    document.getElementById('viewUsername').value = username;
    document.getElementById('viewAge').value = age;
    document.getElementById('viewRoles').value = roles;
    document.getElementById('deleteUserId').value = userId;
    removeBackdrop();
}

// Функция для удаления backdrop и восстановления прокрутки
function removeBackdrop() {
    const backdrop = document.querySelector('.modal-backdrop');
    if (backdrop) {
        backdrop.remove();
    }

    // Восстанавливаем прокрутку страницы
    document.body.style.overflow = 'auto';
    document.body.style.paddingRight = '0';
}
