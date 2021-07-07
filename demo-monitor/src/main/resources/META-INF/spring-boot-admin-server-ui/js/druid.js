

var applications = null;
$(document).ready(function () {
    reloadApplications();
});


function reloadApplications() {
    var result = reqSync("/api/applications", "get");
    applications = result;
    initSelect("#selectServer", applications, "name");
    var serviceUrl = getSelectData("#selectServer", applications).serviceUrl;
    $("#contentIfr").attr("src", serviceUrl + "druid/index.html");
}

$("#selectServer").change(function () {
    var serviceUrl = getSelectData("#selectServer", applications).serviceUrl;
    $("#contentIfr").attr("src", serviceUrl + "druid/index.html");
    reinitIframe();
});

$("#getServer").click(function () {
    reloadApplications();
});
$("#openInNewWindow").click(function () {
    var serviceUrl = getSelectData("#selectServer", applications).serviceUrl;
    window.open(serviceUrl + "druid/index.html");
});


function getSelectData(selectUi, list) {
    var index = $(selectUi).val();
    if (index == "") {
        return {};
    }
    return list[parseInt(index)];
}

function initSelect(uiSelect, list, key) {
    $(uiSelect).html('');
    // $(uiSelect).prepend("<option value=''>请选择</option>"); //添加第一个option值
    for (var i = 0; i < list.length; i++) {
        $(uiSelect).append("<option value=" + i + ">" + list[i][key] + "</option>");
    }
}

function reqSync(url, method) {
    var result = null;
    $.ajax({
        url: url,
        type: method,
        async: false, //使用同步的方式,true为异步方式
        headers: {
            'Content-Type': 'application/json;charset=utf8;',
        },
        success: function (data) {
            console.log(data);
            result = data;
        },
        error: function (data) {
            console.log("error");
        }
    });
    return result;
}

reinitIframe();
window.onresize = function () {
    reinitIframe();
}

function reinitIframe() {
    var iframe = document.getElementById("contentIfr");
    try {
        var winWidth = 1000;
        var winHeight = 1000;
        // 获取窗口宽度
        if (window.innerWidth)
            winWidth = window.innerWidth;
        else if ((document.body) && (document.body.clientWidth))
            winWidth = document.body.clientWidth;
        // 获取窗口高度
        if (window.innerHeight)
            winHeight = window.innerHeight;
        else if ((document.body) && (document.body.clientHeight))
            winHeight = document.body.clientHeight;
        // 通过深入 Document 内部对 body 进行检测，获取窗口大小
        /* if (document.documentElement && document.documentElement.clientHeight && document.documentElement.clientWidth) {
             winHeight = document.documentElement.clientHeight;
             winWidth = document.documentElement.clientWidth;
         }*/
        iframe.height = winHeight - 25;
        iframe.width = winWidth - 50;
        console.log(iframe.height + "-" + iframe.width);
    } catch (ex) {
    }
}