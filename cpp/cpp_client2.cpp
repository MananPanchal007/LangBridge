// cpp/cpp_client2.cpp
// Calls the Python processor /process endpoint to transform a doc.
// Compile: g++ cpp_client2.cpp -lcurl -o cpp_client2

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
    std::string payload = "{\"doc\":\"hello from cpp2\",\"analysis_text\":\"check grammar\"}";
    struct curl_slist *headers = NULL;
    headers = curl_slist_append(headers,"Content-Type: application/json");
    curl_easy_setopt(curl, CURLOPT_URL, "http://localhost:8006/process");
    curl_easy_setopt(curl, CURLOPT_POSTFIELDS, payload.c_str());
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    std::string response;
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_cb);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
    CURLcode res = curl_easy_perform(curl);
    if(res != CURLE_OK){ std::cerr<<"Processor call failed\n"; return 1; }
    std::cout<<"Processor response: "<<response<<"\n";
    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);
    return 0;
}
