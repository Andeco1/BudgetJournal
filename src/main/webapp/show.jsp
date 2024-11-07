<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Список пользователей</title>
</head>
<body>
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