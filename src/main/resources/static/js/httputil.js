/**
 * author ouliuying
 */

function showOverLay() {
    $("#bg_overlay_httputil__id").show();
}

function hideOverLay() {
    $("#bg_overlay_httputil__id").hide();
}

function request(method, url, data, successFn, failFn, showWait) {
    $.ajax(url, {
        data: data,
        beforeSend: function() {
            if (showWait) {
                showOverLay()
            }
        },
        complete: function() {
            if (showWait) {
                hideOverLay()
            }
        },
        method: method,
        success: function(data) {
            if (showWait) {
                hideOverLay()
            }
            successFn(data)
        },
        error: function() {
            if (showWait) {
                hideOverLay()
            }
            failFn()
        }
    })
}

function post(url, data, success, fail, showWait) {
    if (success !== undefined) {
        if (typeof(success) != "function") {
            showWait = success;
        } else if (fail !== undefined && typeof(fail) != "function") {

            showWait = fail;

        }
    } else {
        success = function() {}
        fail = function() {}
        showWait = false;
    }
    success = success || function() {}
    fail = fail || function() {}
    showWait === undefined && (showWait = true)
    request("POST", url, data, success, fail, showWait)
}

function get(url, data, success, fail, showWait) {
    if (success !== undefined) {
        if (typeof(success) != "function") {
            showWait = success;
        } else if (fail !== undefined && typeof(fail) != "function") {

            showWait = fail;

        }
    } else {
        success = function() {}
        fail = function() {}
        showWait = false;
    }
    success = success || function() {}
    fail = fail || function() {}

    showWait === undefined && (showWait = true)

    request("GET", url, data, success, fail, showWait)
}

$.http = {
    post: post,
    get: get
}

$(function() {
    $("#bg_overlay_httputil__id").remove();
    $("#bg_overlay_httputil__id__style").remove();
    $('head').append(`
    <style id='bg_overlay_httputil__id__style'>
        #bg_overlay_httputil__id{
            position:absolute;
            top:0;
            left:0;
            width:100%;
            height:100%;
            z-index:999999;
            
        }
        #bg_overlay_httputil__id__showarea{
            position:absolute;
            width:120px;
            height:40px;
            background:#000;
            left:50%;
            top:50%;
            transform: translate(-50%,-50%);
            color:white;  
            text-align:center;
        }
    </style>
    `);
    //$("#bg_overlay_httputil__id__style").remove();
    $(`
      <div id="bg_overlay_httputil__id" style='display:none;'>
        <div id="bg_overlay_httputil__id__showarea">
            简单动画:）
        </div>
      </div>
    `).appendTo('body');
})