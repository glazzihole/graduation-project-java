$(function () {
    $("search-content").attr("placeholder", "请输入查询内容");
});

/**
 * 根据search-stu-option下拉列表的内容，改变搜索框中的提示内容
 */
function changePlaceholder(){
    var functionValue = $("#funciton-option option:selected").val();
    switch (functionValue) {
        case "1":
            $("#search-content").attr("placeholder", "请输入查询内容");
            break;
        case "2":
            $("#search-content").attr("placeholder", "单词A,单词A词性,单词B,单词B词性,单词C,单词C词性(用逗号隔开，若无内容则输入空格)");
            break;
        case "3":
            $("#search-content").attr("placeholder", "搭配,搭配中的词性(如：open door, VB NN)");
            break;
    }
}

/**
 * 查询功能
 */
function search(currentPage) {

    var searchContent = $("#search-content").val();
    var functionValue = $("#funciton-option option:selected").val();
    var corpus = $("#corpus-option option:selected").val();
    var url;
    var type;
    var data;
    switch (functionValue) {
        case "1":
            type = "GET";
            url = "/corpus/sentence";
            data = {
                patt: "<s/> containing \"" + searchContent + "\"",
                corpus: corpus,
                pageNumber:currentPage,
                pageSize:10
            };
            break;
        case "2":
            type = "POST";
            url = "/corpus/collocation";
            var contentArray = searchContent.split(",");
            if (contentArray.length == 4) {
                var firstWord = check(contentArray[0]);
                var firstPos = check(contentArray[1]);
                var secondWord = check(contentArray[2]);
                var secondPos = check(contentArray[3]);
                data = JSON.stringify({
                    "first_word": firstWord,
                    "first_pos": firstPos,
                    "second_word": secondWord,
                    "second_pos": secondPos,
                    "corpus": corpus,
                    "pageNumber":1,
                    "pageSize":10
                });
            } else if (contentArray.length == 6) {
                var firstWord = check(contentArray[0]);
                var firstPos = check(contentArray[1]);
                var secondWord = check(contentArray[2]);
                var secondPos = check(contentArray[3]);
                var thirdWord = check(contentArray[4]);
                var thirdPos = check(contentArray[5]);
                data = JSON.stringify({
                    "first_word": firstWord,
                    "first_pos": firstPos,
                    "second_word": secondWord,
                    "second_pos": secondPos,
                    "third_word": thirdWord,
                    "third_pos": thirdPos,
                    "corpus": corpus,
                    "pageNumber":1,
                    "pageSize":10
                });
            }
            break;

        case "3":
            type = "GET";
            url = "/corpus/collocation/synonym/recommend";
            var contentArray = searchContent.split(",");
            var wordPair = contentArray[0];
            var posPair = contentArray[1];
            data = {
                word_pair: wordPair,
                pos_pair:posPair,
                corpus:corpus
            };
            break;
    }
    $(".waiting-background").css("display","block");
    $.ajax({
        url: url,
        type: type,
        data: data,
        contentType: 'application/json;charset=UTF-8',
        dataType: "json",
        success: function (result) {
            $(".waiting-background").css("display","none");
            if (result["code"] != 200) {
                layer.msg("查询失败，请检查参数输入或稍后再试");
            } else {
                // 显示查询内容
                switch (functionValue) {
                    case "1":
                        $("#sentence-result").find("tbody").empty();
                        $("#sentence-result").css("display", "block");
                        $("#collocation-result").css("display", "none");
                        var html = '';
                        $.each(result['data']['page'], function (index, content) {
                            html += '<tr>' +
                                '<td>' + content['sentence'] +
                                '<br/>' +
                                '<div class = "assist-reading" id = "div_' + content['id'] + '" style="display: none">' +
                                '</div>' +
                                '</td>' +
                                '<td>' +
                                '<button id="assist-reading" class="am-btn am-btn-default am-btn-xs am-text-secondary" onclick="assistReading(\'' + handleQuote(encodeURI(content['sentence'])) + '\', ' + content['id'] + ');return false;">' +
                                '<span class="am-icon-book"></span> 辅助阅读</button><br/>' +
                                '<span id="assist-reading-close_' + content['id'] + '" class="assist-reading-close" style="display: none"><a href="javascript:void(0)" onclick="assistReadingClose(' + content['id'] + ')">收起</span>' +
                                '</td>' +
                                '</tr>';
                        });
                        $("#sentence-result").find("tbody").html(html);
                        break;
                    case "2":
                        $("#collocation-result").find("tbody").empty();
                        $("#sentence-result").css("display", "none");
                        $("#collocation-result").css("display", "block");
                        var html = '';
                        $.each(result['data']['page'], function (index, content) {
                            html += '<tr>' +
                                '<td class="collocation-column">' +
                                content['first_word'] +
                                ' ' +
                                content['second_word'] +
                                ' ' +
                                content['third_word'] +
                                '</td>' +
                                '<td class="freq-column">' +
                                content['freq'] +
                                '</td>';
                            html += '</tr>'
                        });
                        $("#collocation-result").find("tbody").html(html);
                        break;
                    case "3":
                        $("#collocation-result").find("tbody").empty();
                        $("#sentence-result").css("display", "none");
                        $("#collocation-result").css("display", "block");
                        var html = '';
                        $.each(result['data'], function (index, content) {
                            html += '<tr>' +
                                '<td class="collocation-column">' +
                                content['first_word'] +
                                ' ' +
                                content['second_word'] +
                                ' ' +
                                content['third_word'] +
                                '</td>' +
                                '<td class="freq-column">' +
                                content['freq'] +
                                '</td>';
                            html += '</tr>'
                        });
                        $("#collocation-result").find("tbody").html(html);
                        break;
                }// switch
                laypage({
                    cont: 'pagebar',//容器。值支持id名、原生dom对象，jquery对象。【如该容器为】：<div id="page1"></div>
                    pages: result['data']['totalPages'],//通过后台拿到的总页数
                    skin: '#6665fe',
                    curr: currentPage, //当前页
                    jump: function (obj, first) {//出发分页后的回调
                        if (!first) {//点击跳页触发函数自身，并传递当前页：obj.curr
                            search(obj.curr);
                        }
                    }
                });
            }
        },
        error: function () {
            layer.msg("查询失败，请检查参数输入或稍后再试");
        }
    });
}

/**
 * 检查是否为空
 * @param content
 * @returns {null|*}
 */
function  check(content) {
    if (content != null && content != " ") {
        return content;
    } else {
        return null;
    }
}

/**
 * 处理单引号
 *
 * @param sentence
 * @returns {void | string}
 */
function handleQuote(sentence) {
    sentence = sentence.replace(/\'/g, "\\'");
    return sentence;
}

/**
 * 辅助阅读
 * @param sentence
 * @param id
 */
function assistReading(sentence, id) {
    sentence = decodeURI(sentence);
    $("#div_" + id).css("display", "block");
    $("#div_" + id).empty();
    $.ajax({
        url: "/support/google-translate",
        type: "POST",
        data: {
            text: sentence,
            from: "en",
            to: "zh",
        },
        // contentType: 'application/json;charset=UTF-8',
        dataType: "json",
        success: function (result1) {

            if (result1["code"] != 200) {
                layer.msg("机器翻译结果查询失败，请稍后再试");
            } else {
                // 显示机器翻译内容
                $("#div_" + id).html("机器翻译结果：" + result1["data"] + "<br/>");

                // 请求句型判断
                $.ajax({
                    url: "/corpus/sentence/pattern",
                    type: "POST",
                    data: {
                        sentence : sentence
                    },
                    // contentType: 'application/json;charset=UTF-8',
                    // dataType: "json",
                    success: function (result2) {
                        if (result2["code"] != 200) {
                            layer.msg("句型分析失败，请稍后再试");
                        } else {
                            // 显示句型判断结果
                            $.each(result2['data'], function (index, content) {
                                switch (content["type"]) {
                                    case 1:
                                        $("#div_" + id).append("主语从句：" + content["clauseContent"] + "<br/>");
                                        break;
                                    case 2:
                                        $("#div_" + id).append("宾语从句：" + content["clauseContent"] + "<br/>");
                                        break;
                                    case 3:
                                        $("#div_" + id).append("定语从句或同位语从句：" + content["clauseContent"] + "<br/>");
                                        break;
                                    case 4:
                                        $("#div_" + id).append("表语从句：" + content["clauseContent"] + "<br/>");
                                        break;
                                    case 5:
                                        $("#div_" + id).append("状语从句：" + content["clauseContent"] + "<br/>");
                                        break;
                                }
                            });
                            // 请求简单句
                            $.ajax({
                                url: "/corpus/sentence/simple-sentence",
                                type: "POST",
                                data: {
                                    sentence : sentence
                                },
                                // contentType: 'application/json;charset=UTF-8',
                                // dataType: "json",
                                success: function (result3) {
                                    if (result3["code"] != 200) {
                                        layer.msg("简单句提取失败，请稍后再试");
                                    } else {
                                        // 显示简单句提取结果
                                        $("#div_" + id).append("简单句提取结果：");
                                        $.each(result3['data'], function(index, content) {
                                            $("#div_" + id).append(content + ", ");
                                        });
                                        $("#div_" + id).append("<br/>");
                                        // 请求句子级别结果
                                        $.ajax({
                                            url: "/corpus/sentence/rank-num",
                                            type: "POST",
                                            data: {
                                                sentence : sentence,
                                            },
                                            // contentType: 'application/json;charset=UTF-8',
                                            // dataType: "json",
                                            success: function (result4) {
                                                if (result4["code"] != 200) {
                                                    layer.msg("句子等级分析失败，请稍后再试");
                                                } else {
                                                    // 显示句子等级结果
                                                    $("#div_" + id).append("句子等级为：" + transToZh(result4["data"]));
                                                }
                                            },
                                            error: function () {
                                                layer.msg("句子等级分析失败，请稍后再试");
                                            }
                                        });
                                    }
                                },
                                error: function () {
                                    layer.msg("简单句提取失败，请稍后再试");
                                }
                            });
                        }
                    },
                    error: function () {
                        layer.msg("句型分析失败，请稍后再试");
                    }
                });
            }
        },
        error: function () {
            layer.msg("机器翻译结果查询失败，请稍后再试");
        }
    });
    $("#assist-reading-close_" + id).css("display", "block");
}

function assistReadingClose(id) {
    $("#div_" + id).css("display", "none");
    $("#assist-reading-close_" + id).css("display", "none");
}

/**
 * 将等级指数转换为中文
 * @param rankNum
 * @returns {string}
 */
function transToZh(rankNum) {
    var result;
    switch (rankNum) {
        case 1:
            result = "大学英语四级";
            break;
        case 2:
            result = "大学英语六级";
            break;
        case 3:
            result = "专业英语四级";
            break;
        case 4:
            result = "托福/雅思";
            break;
        case 5:
            result = "专业英语八级";
            break;
        case 6:
            result = "GRE/GMAT";
            break;
    }
    return result;
}