package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.util.svm.svm_predict;

/**
 * @author HU Gailei
 * @date 2019/3/12
 * <p>
 * description: SVM分类训练及测试
 * </p>
 **/
public class SVMTrainAndTest {
    public static void main(String[] args) throws Exception{
        // 训练集 和 SVM训练模型
        String[] arg = { "E:\\毕业论文相关\\作文\\阅读\\阅读理解\\训练集\\手动分级后的\\结果统计\\svm_training.txt",
                "E:\\毕业论文相关\\作文\\阅读\\阅读理解\\训练集\\手动分级后的\\结果统计\\svm_model.txt" };

        System.out.println("........SVM运行开始..........");
        long start=System.currentTimeMillis();
        // 训练
//        svm_train.main(arg);
        System.out.println("用时:"+(System.currentTimeMillis()-start));
        // 测试数据、调用训练模型、预测结果
        String[] parg = { "E:\\毕业论文相关\\作文\\阅读\\阅读理解\\测试集\\测试结果\\svm_training.txt",
        "E:\\毕业论文相关\\作文\\阅读\\阅读理解\\训练集\\手动分级后的\\结果统计\\svm_model.txt",
        "E:\\毕业论文相关\\作文\\阅读\\阅读理解\\训练集\\手动分级后的\\结果统计\\svm_text.txt" };
        //预测
        svm_predict.main(parg);

    }


}
