<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Список пользователей</title>
</head>
<body>
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
</body>
</html>