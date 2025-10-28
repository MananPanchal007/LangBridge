// cpp/cpp_client1.cpp
// Uses libcurl to GET token from AuthService and POST to JavaServer
// Compile: g++ cpp_client1.cpp -lcurl -o cpp_client1

#include <curl/curl.h>
#include <iostream>
#include <string>

static size_t write_cb(void* data, size_t size, size_t nmemb, void* userp){
    ((std::string*)userp)->append((char*)data, size*nmemb);
    return size*nmemb;
}

int main(){
    CURL *curl = curl_easy_init();
    if(!curl) return 1;

    std::string token;
    curl_easy_setopt(curl, CURLOPT_URL, "http://localhost:8001/auth?user=cpp_client");
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_cb);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &token);
    CURLcode res = curl_easy_perform(curl);
    if(res != CURLE_OK){ std::cerr<<"Auth request failed\n"; return 1; }
    std::cout<<"Token: "<<token<<"\n";

    std::string payload = "{\"from\":\"cpp_client\",\"token\":\""+token+"\"}";
    struct curl_slist *headers = NULL;
    headers = curl_slist_append(headers,"Content-Type: application/json");
    curl_easy_setopt(curl, CURLOPT_URL, "http://localhost:8005/javaprocess");
    curl_easy_setopt(curl, CURLOPT_POSTFIELDS, payload.c_str());
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    std::string response;
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
    res = curl_easy_perform(curl);
    if(res != CURLE_OK){ std::cerr<<"POST failed\n"; return 1; }
    std::cout<<"Response: "<<response<<"\n";
    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);
    return 0;
}
