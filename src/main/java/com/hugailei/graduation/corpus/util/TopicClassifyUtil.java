package com.hugailei.graduation.corpus.util;

import com.hugailei.graduation.corpus.dto.TopicDto;
import com.hugailei.graduation.corpus.enums.Topic;
import com.mayabot.mynlp.fasttext.FastText;
import com.mayabot.mynlp.fasttext.FloatStringPair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/12/21
 * <p>
 * description: 主题分类
 * </p>
 **/
public class TopicClassifyUtil {
    private static final String MODEL_PATH = "E:\\毕业论文相关\\fasttext-train\\model.bin";
    private static FastText FAST_TEXT = FastText.loadModel(MODEL_PATH);
    private static final String LABEL_PREFIX = "__label__";

    /**
     * 获取文章的主题标号及对应概率
     *
     * @param text
     * @return
     */
    public static LinkedHashMap<Integer, Float> getTopicNumAndProbability(String text) {
        List<FloatStringPair> predict = FAST_TEXT.predict(Arrays.asList(text.split(" ")), 5);
        LinkedHashMap<Integer, Float> topicNumAndProbability = new LinkedHashMap<>();
        int count = 1;
        for (FloatStringPair pair : predict) {
            if (count == 1 && pair.first < 0.2) {
                topicNumAndProbability.put(Topic.OTHER.getTopicNum(), pair.first);
                break;
            }
            String label = pair.second;
            int num = Integer.valueOf(label.replaceAll(LABEL_PREFIX, ""));
            float prob = pair.first;

            topicNumAndProbability.put(num, prob);
            count ++;
        }
        return topicNumAndProbability;
    }

    /**
     * 获取文章的主题名称及对应概率
     *
     * @param text
     * @return
     */
    public static LinkedHashMap<String, Float> getTopicNameAndProbability(String text) {
        List<FloatStringPair> predict = FAST_TEXT.predict(Arrays.asList(text.split(" ")), 5);
        LinkedHashMap<String, Float> topicNumAndProbability = new LinkedHashMap<>();
        int count = 1;
        for (FloatStringPair pair : predict) {
            if (count == 1 && pair.first < 0.2) {
                topicNumAndProbability.put(Topic.OTHER.getTopicName(), pair.first);
                break;
            }
            String label = pair.second;
            float prob = pair.first;
            int num = Integer.valueOf(label.replaceAll(LABEL_PREFIX, ""));
            Topic topic = Topic.getEnumBycode(Topic.class, num);

            topicNumAndProbability.put(topic.getTopicName(), prob);
            count ++;
        }
        return topicNumAndProbability;
    }

    /**
     * 获取文章的主题信息列表
     *
     * @param text
     * @return
     */
    public static List<TopicDto> getTopicInfoList(String text) {
        List<FloatStringPair> predict = FAST_TEXT.predict(Arrays.asList(text.split(" ")), 5);
        List<TopicDto> topicInfoList = new ArrayList<>();
        int count = 1;
        for (FloatStringPair pair : predict) {
            if (count == 1 && pair.first < 0.2) {
                topicInfoList.add(new TopicDto(Topic.OTHER.getTopicName(), Topic.OTHER.getTopicNum(), null));
                break;
            }

            float prob = pair.first;
            int topicNum = Integer.valueOf(pair.second.replaceAll(LABEL_PREFIX, ""));
            Topic topic = Topic.getEnumBycode(Topic.class, topicNum);
            String topicName = topic.getTopicName();
            topicInfoList.add(new TopicDto(topicName, topicNum, prob));
            count ++;
        }
        return topicInfoList;
    }
}
