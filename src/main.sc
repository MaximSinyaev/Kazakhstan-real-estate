require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
require: localPatterns.sc

theme: /

    state: Start
        q!: $regex</start>
        a: Салам Алейкум, @я бот Максат и я помогу тебе найти квартиру по твоим настройкам самым первым.
        buttons:
            "Создать фильтр" -> /create_or_update_user
        event: noMatch || toState = "./"

    state: Hello
        intent!: /привет
        a: Привет привет

    state: Bye
        intent!: /пока
        a: Пока пока

    state: NoMatch
        event!: noMatch
        a: Я не понял. Вы сказали: {{$request.query}}

    state: Match
        event!: match
        a: {{$context.intent.answer}}

    state: create_or_update_user
        a: Отправь мне ссылку поиска со всеми включенными фильтрами, которую я смогу парсить и отправлять тебе все свежие кварьтры
        a: >Account id: {{$request.accountId}}
            > Channel user id: {{$request.channelUserId}}
            > User From data: id: {{$request.userFrom.id}} First name: {{$request.userFrom.FirstName}} Last name:  {{$request.userFrom.LastName}}
        HttpRequest: 
            url = http://51.250.18.104:8088/add_user
            method = POST
            dataType = application/json
            body = {
                "account_id": "{{$request.accountId}}",
                "user_chat_id": "{{$request.userFrom.id}}"
                }
            timeout = 0
            headers = [{"name":"content-type","value":"application\/json"}]
            vars = []
        intent:
            
        event: noMatch || toState = "/create_filter", onlyThisState = true

        state: create_filter
            event: noMatch || fromState = "/create_or_update_user"
            a: Отлично, мы записали твои предпочтения, я уведомлю тебя сразу как найду подходящие объявления!
            HttpRequest: 
                url = https://test-kz-real-estate.free.beeceptor.com
                method = POST
                dataType = 
                body = {
                    "account_id": {{$request.accountId}},
                    "user_chat_id": {{$request.userFrom.id}},
                    "first_name": {{$request.userFrom.FirstName}},
                    "last_name": {{$request.userFrom.LastName}},
                    "filters_url": {{$request.rawRequest}}
                    }
                timeout = 0
                headers = [{"name":"","value":""}]
                vars = [{"name":"","value":""}]