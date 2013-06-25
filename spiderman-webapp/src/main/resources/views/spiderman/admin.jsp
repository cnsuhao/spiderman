<%@ page language="java" contentType="text/html; charset=utf-8"  pageEncoding="utf-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<html>
	<head>
		<title>Spiderman Control Panel</title>
		<style type="text/css" media="screen">
		#wrap{margin:10px auto; width:80%; height:99%;}
		.unit{padding:5px; border:solid 1px #000; height:auto; margin-top:3px; clear:both;}	
		.unit label{text-align:right;width:100px; line-height: 30px; display:inline-block;}
		.unit input{line-height: 30px; width:100px; height:30px;}
		.log {font-size:11px; font-family:Courier; height:65%; overflow: auto; background:black;}
		.log p{padding:2px;margin:0;}
		</style>
	</head>
	<body>
		<div id="wrap">
			<div class="unit">
				<label>Schedule:</label>
				<input <c:choose><c:when test="${scheduleStatus == '1'}"> disabled="disabled" </c:when> <c:otherwise> id="schedule_time" type="text" value="${time}" </c:otherwise> </c:choose> />
				<label>delay:</label>
				<input <c:choose><c:when test="${scheduleStatus == '1'}"> disabled="disabled" </c:when> <c:otherwise> id="schedule_delay" type="text" value="${delay}" </c:otherwise> </c:choose> />
				<input <c:choose><c:when test="${scheduleStatus == '1'}"> disabled="disabled" </c:when> <c:otherwise> onclick="javascript:schedule()" </c:otherwise> </c:choose> type="button" value="start" />
				<input <c:choose><c:when test="${scheduleStatus == 0}"> disabled="disabled" </c:when> <c:otherwise> onclick="window.location='${BaseURL}spiderman/cancel_schedule'" </c:otherwise> </c:choose> type="button" value="stop" />
			</div>
			<div class="unit">
				<label>Refresh:</label>
				<input <c:choose><c:when test="${refreshStatus == '1'}"> disabled="disabled" </c:when> <c:otherwise> id="refresh_seconds" type="text" value="${refreshSeconds}" </c:otherwise> </c:choose> />
				<input <c:choose><c:when test="${refreshStatus == '1'}"> disabled="disabled" </c:when> <c:otherwise> onclick="javascript:refresh()" </c:otherwise> </c:choose> type="button" value="refresh" />
				<input <c:choose><c:when test="${refreshStatus == 0}"> disabled="disabled" </c:when> <c:otherwise> onclick="window.location='${BaseURL}spiderman/cancel_refresh'" </c:otherwise> </c:choose> type="button" value="cancel" />
			</div>
			
			<div class="unit">
				<label>Logs:</label>
				<input type="button" value="clear logs" onclick="window.location='${BaseURL}spiderman/clear_logs'"/>
			</div>
			<div id="logs" class="unit log">
				<p>&nbsp;</p>
				${logs}
			</div>
		</div>
	</body>
	<script type="text/javascript" charset="utf-8">
		var logDiv = document.getElementById('logs'); 
		logDiv.scrollTop = logDiv.scrollHeight;
		scroll(0, 100000);
		function refresh(){
			var seconds = document.getElementById('refresh_seconds').value;
			window.location='${BaseURL}spiderman/refresh?s='+seconds;
		}
		function schedule(){
			var time = document.getElementById('schedule_time').value;
			var delay = document.getElementById('schedule_delay').value;
			window.location='${BaseURL}spiderman/schedule?time='+time+'&delay='+delay;
		}
	</script>
	<c:choose>
		<c:when test="${refreshStatus == '1'}">
		<script type="text/javascript" charset="utf-8">
			var seconds = ${refreshSeconds};
			if (seconds && seconds > 0) {
				var interval = window.setInterval(function(){
					window.location.reload();
				}, seconds * 1000);
			}
		</script>
		</c:when>
	</c:choose>
</html>