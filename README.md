 <h1 align = "center">Explore with me</h1>


<h3>Приложение — афиша. 
В этой афише можно предложить какое-либо событие от выставки до похода в кино
и собрать компанию для участия в нём.</h3>


<details>
  <summary>Содержание</summary>
  <ol>
    <li>
      <a href="#о-проекте">О проекте</a>
      <ul>
        <li><a href="#создан-при-помощи">Создан при помощи</a></li>
      </ul>
    </li>
    <li>
      <a href="#запуск-проекта">Запуск проекта</a>
      <ul>
        <li><a href="#приготовления">Приготовления</a></li>
        <li><a href="#установка">Установка</a></li>
      </ul>
    </li>
    <li><a href="#использование">Использование</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## О проекте

Проект основан на микросервисной архитектуре с использованием Docker контейнеров.
#### В проекте есть разделение эндпоинтов на:
* Публичные - доступные любому пользователю
* Приватные - доступ только у зарегестрированных пользователей
* Для админов - реализуют возможность модерации.

#### Сервис предоставляет следующий функционал:
* Просмотр публичных ивентов
* Добавление нового ивента
* Регистрация на имеющийся ивент
* Регистрация пользователя
* Изменения админом статуса ивентов (одобрение/отклонение)
* Добавление категории для ивентов
* Просмотр подборок ивентов

#### Проект состоит из 3-х микросервисов:
* Ewm-service - основной сервис для работы с приложением
* Stats-service - сервис сбора статистики просмотров ивентов
* DB - база данных
  Каждый микросервис поднимается в своём докер контейнере.

<p align="right">(<a href="#readme-top">к заглавию</a>)</p>



### Создан при помощи

* ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
* ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
* ![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
* ![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)

<p align="right">(<a href="#readme-top">к заглавию</a>)</p>



<!-- GETTING STARTED -->
## Запуск проекта

Далее описаны требования для запуска проекта на локальной машине:

### Приготовления

Для работы приложение требуется установленный и запущенный Docker daemon. Для проверки его наличия введите следующую команду в консоли (Windows)
* cmd
  ```sh
  docker version
  ```
Если выводится информация о установленной системе, переходим к следующему шагу.

### Установка

_Далее описаны пункты для запуска проекта_

1. Клонируйте репозиторий
   ```cmd
   git clone https://github.com/kozulis/java-explore-with-me.git
   ```
2. Перейдите в корневую папку проекта
   ```cmd
   cd {путь да корневой директории}/java-explore-with-me
   ```
4. Вызовите утилиту что бы поднять все контейнеры
   ```cmd
   docker-compose up
   ```

<p align="right">(<a href="#readme-top">к заглавию</a>)</p>



<!-- USAGE EXAMPLES -->
## Использование

_Postman-коллекция с примерами запросов - [Ссылка](https://github.com/kozulis/java-explore-with-me/tree/main/postman)_

<p align="right">(<a href="#readme-top">к заглавию</a>)</p>

<!-- CONTACT -->
## Контакты

Милованов Алексей - Телеграм(@alekseymvnv) - kozulis@yandex.ru

Ссылка на проект: [https://github.com/kozulis/java-explore-with-me](https://github.com/kozulis/java-explore-with-me)

<p align="right">(<a href="#readme-top">к заглавию</a>)</p>

