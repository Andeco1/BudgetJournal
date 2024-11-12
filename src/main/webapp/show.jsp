<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<style>
   <%@include file='css/style.css' %>
</style>
<head>
<title>Список пользователей</title>
</head>
<body>
<section class="left_part">
  <article class="form">
<form action="" method="post">
  <div>Введите период</div>
  <input type="text" name="from" value="${param.from}" placeholder="yyyy-mm-dd">
  <input type="text" name="to" value="${param.to}" placeholder="yyyy-mm-dd">
  <div>Выберите категорию</div>
  <c:forEach var="category" items="${categories}">
    <input type ="checkbox" name="categories" value="${category}">${category} <br>
   </c:forEach>
    <div>Выберите тип операции</div>
    <select name="operation" id="operation">
      <option value="any">Любой</option>
      <option value="+">Пополнение</option>
      <option value="-">Списание</option>
    </select>
    <input type="submit" value="Показать">
  </form>
  </article>
  <article class="table">
  <table border="2">
    <tr>
      <td>Категория</td>
      <td>Дата</td>
      <td>Сумма</td>
    </tr>
  <c:forEach items="${records}" var = "record">
      <tr>
        <td>${record.getCategory_name()}</td>
        <td>${record.getOperation_date()}</td>
        <td>${record.getTotal()}</td>
      </tr>
  </c:forEach>
  </table>
  </article>
</section>
<section class ="right_part">
<article class="statistics">
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  <c:if test = "${not empty statistics_percentage}">
  <canvas id="creditingChart" width="400" height="200"></canvas>
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
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1
                }]
            }
        });
  </script>
    <canvas id="myChart" width="600" height="500"></canvas>
   <script>
         var categories = [
                 <c:forEach var="category" items="${categories_names}" varStatus="status">
                     '${category}'<c:if test="${!status.last}">,</c:if>
                 </c:forEach>
        ];
        var labels = [<c:forEach var="date" items="${statistics_dates}" varStatus="status">'${date}'<c:if test="${!status.last}">,</c:if></c:forEach>];
        var datasets = [];
        var colors = [
            'rgba(255, 99, 132, 1)', 'rgba(54, 162, 235, 1)', 'rgba(75, 192, 192, 1)',
            'rgba(153, 102, 255, 1)', 'rgba(255, 159, 64, 1)', 'rgba(199, 199, 199, 1)'
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
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
   </script>
  </c:if>
</article>
</section>
</body>
</html>