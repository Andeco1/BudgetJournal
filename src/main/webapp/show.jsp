<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Бюджетный журнал</title>
    <style>
        <%@include file='css/style.css' %>
    </style>
</head>
<body>
    <div class="container">
        <section class="left_part">
            <article class="form">
                <h2>Фильтры</h2>
                <form action="" method="post">
                    <div>Период</div>
                    <input type="date" name="from" value="${param.from}" placeholder="yyyy-mm-dd">
                    <input type="date" name="to" value="${param.to}" placeholder="yyyy-mm-dd">
                    
                    <div>Категории</div>
                    <div class="categories-grid">
                        <c:forEach var="category" items="${categories}">
                            <label class="category-label">
                                <input type="checkbox" name="categories" value="${category}">
                                <span>${category}</span>
                            </label>
                        </c:forEach>
                    </div>
                    
                    <div>Тип операции</div>
                    <select name="operation" id="operation">
                        <option value="any">Любой</option>
                        <option value="+">Пополнение</option>
                        <option value="-">Списание</option>
                    </select>
                    
                    <input type="submit" value="Применить фильтры">
                </form>
            </article>
            
            <article class="table">
                <h2>Записи</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Категория</th>
                            <th>Дата</th>
                            <th>Сумма</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${records}" var="record">
                            <tr>
                                <td>${record.getCategory_name()}</td>
                                <td>${record.getOperation_date()}</td>
                                <td>${record.getTotal()}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </article>
        </section>
        
        <section class="right_part">
            <article class="statistics">
                <h2>Статистика</h2>
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <c:if test="${not empty statistics_percentage}">
                    <div class="chart-container">
                        <canvas id="creditingChart"></canvas>
                    </div>
                    <div class="chart-container">
                        <canvas id="myChart"></canvas>
                    </div>
                    <script>
                        var labels = [
                            <c:forEach var="category" items="${statistics_categories_names}" varStatus="status">
                                '${category}'<c:if test="${!status.last}">,</c:if>
                            </c:forEach>
                        ];
                        var dataPoints = ${statistics_percentage}
                        var ctx = document.getElementById('creditingChart').getContext('2d');
                        var myChart = new Chart(ctx, {
                            type: 'pie',
                            data: {
                                labels: labels,
                                datasets: [{
                                    label: 'Итого по операциям:',
                                    data: dataPoints,
                                    backgroundColor: [
                                        'rgba(255, 99, 132, 0.2)',
                                        'rgba(54, 162, 235, 0.2)',
                                        'rgba(255, 206, 86, 0.2)',
                                        'rgba(75, 192, 192, 0.2)',
                                        'rgba(153, 102, 255, 0.2)',
                                        'rgba(255, 159, 64, 0.2)'
                                    ],
                                    borderColor: [
                                        'rgba(255, 99, 132, 1)',
                                        'rgba(54, 162, 235, 1)',
                                        'rgba(255, 206, 86, 1)',
                                        'rgba(75, 192, 192, 1)',
                                        'rgba(153, 102, 255, 1)',
                                        'rgba(255, 159, 64, 1)'
                                    ],
                                    borderWidth: 1
                                }]
                            },
                            options: {
                                responsive: true,
                                plugins: {
                                    legend: {
                                        position: 'right',
                                    },
                                    title: {
                                        display: true,
                                        text: 'Распределение по категориям'
                                    }
                                }
                            }
                        });

                        var categories = [
                            <c:forEach var="category" items="${categories_names}" varStatus="status">
                                '${category}'<c:if test="${!status.last}">,</c:if>
                            </c:forEach>
                        ];
                        
                        var datasets = [];
                        var colors = [
                            'rgba(255, 99, 132, 1)', 'rgba(54, 162, 235, 1)', 'rgba(255, 206, 86, 1)',
                            'rgba(75, 192, 192, 1)', 'rgba(153, 102, 255, 1)', 'rgba(255, 159, 64, 1)'
                        ];
                        var colorIndex = 0;
                        <c:forEach var="category_id" items="${categories_id}">
                            var data = [<c:forEach var="dataPoint" items="${categoryData[category_id]}">${dataPoint}<c:if test="${!status.last}">,</c:if></c:forEach>];
                            datasets.push({
                                label: '${categories[category_id]}',
                                data: data,
                                borderColor: colors[colorIndex % colors.length],
                                backgroundColor: colors[colorIndex % colors.length].replace('1)', '0.2)'),
                                borderWidth: 2,
                                fill: false
                            });
                            colorIndex++;
                        </c:forEach>

                        var ctx = document.getElementById('myChart').getContext('2d');
                        var myChart = new Chart(ctx, {
                            type: 'line',
                            data: {
                                labels: labels,
                                datasets: datasets
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
                </c:if>
            </article>
        </section>
    </div>
</body>
</html>