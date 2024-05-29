import requests

def call_api_with_token(url, data, auth_token):
    headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': f'Token {auth_token}'
    }

    try:
        # Make a POST request to the API with the provided headers
        response = requests.post(url, json=data, headers=headers)

        # Check if the request was successful (status code 2xx)
        if response.status_code // 100 == 2:
            print(f"Request successful. Response: {response.json()}")
        else:
            print(f"Request failed. Status code: {response.status_code}, Response: {response.text}")

    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    # Replace 'YOUR_API_URL' with the actual URL of the API endpoint you want to call
    api_url = 'YOUR_API_URL'

    # Replace 'YOUR_JSON_DATA' with the JSON data you want to send in the POST request
    json_data = {
        "msg": "correcting 2.6 release schedule"
    }

    # Replace 'YOUR_TOKEN' with your actual authentication token
    auth_token = 'YOUR_TOKEN'

    # Call the API with the authentication token
    call_api_with_token(api_url, json_data, auth_token)
