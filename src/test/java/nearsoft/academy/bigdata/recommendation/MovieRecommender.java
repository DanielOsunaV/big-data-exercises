/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/**
 *
 * @author jdov1
 */
class MovieRecommender {
    private final HashMap<String, Integer> usersHash = new HashMap();
    private final HashMap<String, Integer> productsHash = new HashMap();
    private final HashMap<Integer, String> inverseProductsHash = new HashMap();
    
    private int countReviews = 0, countUsers = 0, countProducts = 0, productNum, userNum;
    
    MovieRecommender(String path) throws IOException, TasteException{

            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            File reviews = new File("reviews.csv");
            FileWriter fw = new FileWriter(reviews);
            BufferedWriter wr = new BufferedWriter(fw);
            
            String userId = "", productId = "", score;
            String line;
            
            while ((line = br.readLine()) != null) {
                
                if(line.startsWith("product/productId:")) {
                    productId = line.split(" ")[1];
                    if (productsHash.containsKey(productId) == false) {
                        countProducts++;
                        productsHash.put(productId,countProducts);
                        inverseProductsHash.put(countProducts, productId);
                        productNum = countProducts;
                    } else {
                        productNum = productsHash.get(productId);
                    }
                }
                if (line.startsWith("review/userId:")) {
                    userId = line.split(" ")[1];
                    if(usersHash.containsKey(userId) == false) {
                        countUsers++;
                        usersHash.put(userId,countUsers);
                        userNum = countUsers;
                    } else {
                        userNum = usersHash.get(userId);
                    }
                }
                if (line.startsWith("review/score:")) {
                    score = line.split(" ")[1];
                    wr.write(userNum + "," + productNum + "," + score + "\n");
                    countReviews++;
                }
            }
            br.close();
            wr.close();
    }
    
    public int getTotalReviews(){
        return countReviews;
    }
    public int getTotalProducts(){
        return countProducts;
    }
    public int getTotalUsers(){
        return countUsers;
    }
    
    List<String> getRecommendationsForUser(String UserId) throws IOException, TasteException {
        DataModel model = new FileDataModel(new File("reviews.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        
        List <RecommendedItem> recommendations = recommender.recommend(usersHash.get(UserId),3);
        List <String> Response = new ArrayList <String>();
        
        for (RecommendedItem recommendation : recommendations) {
            Response.add(inverseProductsHash.get((int)recommendation.getItemID()));
        }
        
        return Response;
    }
}