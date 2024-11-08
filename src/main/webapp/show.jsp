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
  <c:forEach items="${users}" var = "user">
      <tr>
        <td>${user.getCategory_name()}</td>
        <td>${user.getOperation_date()}</td>
        <td>${user.getTotal()}</td>
      </tr>
  </c:forEach>
  </table>
  </article>
</section>
<section class ="right_part">
<article class="statistics">
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  <canvas id="myChart" width="400" height="200"></canvas>
    <script>
        var labels = [
              <c:forEach var="category" items="${categories}" varStatus="status">
                  '${category}'<c:if test="${!status.last}">,</c:if>
              </c:forEach>
        ];
        var dataPoints = [10, 20, 30, 25, 50, 40,20,33,1];
        var ctx = document.getElementById('myChart').getContext('2d');
        var myChart = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Пользователи',
                    data: dataPoints,
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1
                }]
            }
        });
    </script>
</article>
</section>
</body>
</html>