<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Бюджетный журнал</title>
    <style>
        <%@include file='css/style.css' %>
    </style>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <div class="container">
        <section class="left_part">
            <article class="form">
                <h2>Добавить запись</h2>
                <form action="addRecord" method="post" class="add-record-form">
                    <div class="form-group">
                        <label for="category">Категория:</label>
                        <select name="category" id="category" required>
                            <c:if test="${empty categories}">
                                <option value="">Нет доступных категорий</option>
                            </c:if>
                            <c:forEach items="${categories}" var="category">
                                <option value="${category}">${category}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="date">Дата:</label>
                        <input type="date" name="date" id="date" required>
                    </div>
                    <div class="form-group">
                        <label for="total">Сумма:</label>
                        <input type="number" name="total" id="total" step="0.01" required>
                    </div>
                    <div class="form-group">
                        <label>Тип операции:</label>
                        <div class="radio-group">
                            <label>
                                <input type="radio" name="operation" value="income" checked> Доход
                            </label>
                            <label>
                                <input type="radio" name="operation" value="expense"> Расход
                            </label>
                        </div>
                    </div>
                    <button type="submit" class="btn">Добавить</button>
                </form>

                <h2>Фильтры</h2>
                <form action="main" method="post">
                    <div class="form-group">
                        <label for="from">От:</label>
                        <input type="date" name="from" id="from">
                    </div>
                    <div class="form-group">
                        <label for="to">До:</label>
                        <input type="date" name="to" id="to">
                    </div>
                    <div class="form-group">
                        <label>Категории:</label>
                        <div class="checkbox-group">
                            <c:if test="${empty categories}">
                                <p>Нет доступных категорий</p>
                            </c:if>
                            <c:forEach items="${categories}" var="category">
                                <label>
                                    <input type="checkbox" name="categories" value="${category}"> ${category}
                                </label>
                            </c:forEach>
                        </div>
                    </div>
                    <div class="form-group">
                        <label>Тип операции:</label>
                        <div class="radio-group">
                            <label>
                                <input type="radio" name="operation" value="any" checked> Любой
                            </label>
                            <label>
                                <input type="radio" name="operation" value="+"> Доход
                            </label>
                            <label>
                                <input type="radio" name="operation" value="-"> Расход
                            </label>
                        </div>
                    </div>
                    <button type="submit" class="btn">Применить фильтры</button>
                </form>
            </article>
            
            <article class="table">
                <h2>Записи</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Тип</th>
                            <th>Категория</th>
                            <th>Дата</th>
                            <th>Сумма</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:if test="${empty records}">
                            <tr>
                                <td colspan="4">Нет записей</td>
                            </tr>
                        </c:if>
                        <c:forEach items="${records}" var="record">
                            <tr>
                                <td>${record.operation ? 'Расход' : 'Доход'}</td>
                                <td>${record.category_name}</td>
                                <td>${record.operation_date}</td>
                                <td>${record.total}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </article>
        </section>
        
        <section class="right_part">
            <article class="statistics">
                <h2>Статистика</h2>
                <div class="charts-container">
                    <div class="chart">
                        <canvas id="pieChart"></canvas>
                    </div>
                    <div class="chart">
                        <canvas id="lineChart"></canvas>
                    </div>
                </div>
            </article>
        </section>
    </div>

    <script>
        // Debug information
        console.log('Categories:', ${categories});
        console.log('Statistics Categories:', ${statistics_categories_names});
        console.log('Statistics Percentage:', ${statistics_percentage});
        console.log('Statistics Dates:', ${statistics_dates});
        console.log('Category Data:', ${categoryData});

        // Pie Chart
        const pieCtx = document.getElementById('pieChart').getContext('2d');
        new Chart(pieCtx, {
            type: 'pie',
            data: {
                labels: ${statistics_categories_names},
                datasets: [{
                    data: ${statistics_percentage},
                    backgroundColor: [
                        '#FF6384',
                        '#36A2EB',
                        '#FFCE56',
                        '#4BC0C0',
                        '#9966FF',
                        '#FF9F40'
                    ]
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    title: {
                        display: true,
                        text: 'Распределение по категориям'
                    }
                }
            }
        });

        // Line Chart
        const lineCtx = document.getElementById('lineChart').getContext('2d');
        new Chart(lineCtx, {
            type: 'line',
            data: {
                labels: ${statistics_dates},
                datasets: ${categories_names}.map((category, index) => ({
                    label: category,
                    data: ${categoryData}[index],
                    borderColor: [
                        '#FF6384',
                        '#36A2EB',
                        '#FFCE56',
                        '#4BC0C0',
                        '#9966FF',
                        '#FF9F40'
                    ][index % 6],
                    fill: false
                }))
            },
            options: {
                responsive: true,
                plugins: {
                    title: {
                        display: true,
                        text: 'Динамика по категориям'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Сумма'
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: 'Дата'
                        }
                    }
                }
            }
        });
    </script>
</body>
</html>