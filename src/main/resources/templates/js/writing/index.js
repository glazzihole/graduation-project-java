/**
 * 分析作文并显示
 */
function analysisText() {
    //显示动画遮罩层
    $(".waiting-background").css("display","block");
    var text = $("#article").val();
    $.ajax({
        url: "/corpus/collocation/text-analysis",
        type: "POST",
        data: {
            text: text
        },
        success: function (result1) {
            $(".waiting-background").css("display","none");

            if (result1["code"] != 200) {
                layer.msg("分析出错，请稍后再试");
            }
            else {
                // 显示分析所得内容
                var wordCollocationCount = result1['data']['word_collocation_list'].length;
                var posCollocationCount = result1['data']['pos_collocation_list'].length;
                var html = '您的作文中，共使用了' + wordCollocationCount +
                    '种不同的词汇搭配，搭配抽象形式共' + posCollocationCount + '种。具体数据如下：';
                $("#result-window1").empty();
                $("#result-window1").html(html);

                $("#thead1").css("display", "block");
                $("#thead2").css("display", "block");

                // 验证词汇搭配是否正确
                var wordPairString = '';
                $.each(result1['data']['word_collocation_list'], function (index, content) {
                    wordPairString += assemble1(content['first_word'], content['second_word'], content['third_word']) + ',';
                });
                $.ajax({
                    url: "/corpus/collocation/check",
                    type: "POST",
                    data: {
                        word_pair_list: wordPairString
                    },
                    success: function (result2) {
                        if (result2["code"] != 200) {
                            layer.msg("搭配验证出错，请手动查询");
                        }
                        else {
                            $("#result-window2").find("tbody").empty();
                            var html = '';
                            $.each(result1['data']['word_collocation_list'], function (index, content) {
                                html += '<tr>' +
                                    '<td>' +
                                    assemble2(content['first_word'], content['first_pos']) + ' ' +
                                    assemble2(content['second_word'], content['second_pos']) + ' ' +
                                    assemble2(content['third_word'], content['third_pos']) + ' ' +
                                    '</td>' +
                                    '<td>' + content['freq'] + '</td>' +
                                    '<td>' + transToZh(result2['data'][index]) + '</td>' +
                                    '</tr>';

                            });
                            $("#word-collocation-result").html(html);
                            html = '';
                            $.each(result1['data']['pos_collocation_list'], function (index, content) {
                                html += '<tr>' +
                                    '<td>' +
                                    assemble3(content['first_word'], content['first_pos']) + ' ' +
                                    assemble3(content['second_word'], content['second_pos']) + ' ' +
                                    assemble3(content['third_word'], content['third_pos']) + ' ' +
                                    '</td>' +
                                    '<td>' + content['freq'] + '</td>' +
                                    '</tr>';
                            });
                            $("#pos-collocation-result").html(html);
                        }
                    },
                    error: function () {
                        layer.msg("搭配验证出错，请手动查询");
                    }
                });
            }
        },
        error: function(){
            $(".waiting-background").css("display","none");
            layer.msg("分析出错，请稍后再试");
        }
    });
}

/**
 * 将true或者false变成中文显示
 * @param result
 * @returns {string}
 */
function transToZh(result) {
    if (result || result == "true") {
        return "是";
    }
    else {
        return "存疑";
    }
}

/**
 * 将单词拼接成搭配
 *
 * @param firstWord
 * @param secondWord
 * @param thirdWord
 * @returns {string}
 */
function assemble1(firstWord, secondWord, thirdWord) {
    if (firstWord == null || firstWord == "null") {
        firstWord = "";
    }
    if (secondWord == null || secondWord == "null") {
        secondWord = "";
    }
    if (thirdWord == null || thirdWord == "null") {
        thirdWord = "";
    }
    var result = firstWord + " " + secondWord + " " + thirdWord;
    return result;
}

function assemble2(word, pos) {
    if (word == null || word == "null") {
        return ""
    }
    else {
        return word + "/" + pos;
    }
}

function assemble3(word, pos) {
    if (pos == null || pos == "null") {
        return ""
    }
    else {
        if (word == null || word == "null") {
            return pos
        }
        else {
            return word + "/" + pos;
        }
    }
}