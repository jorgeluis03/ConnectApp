package com.example.connectapp.network;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ApiService {
    @POST("send")
    Call<String> sendMessage(@HeaderMap HashMap<String,String> headers, @Body String messageBody);

    /*Los encabezados son información adicional que puedes incluir en tu solicitud, como tokens de autorización, por ejemplo.*/

    /*Esto indica que el cuerpo de la solicitud HTTP sea de tipo String.*/
}
