require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
require: localPatterns.sc

theme: /

    state: Start
        q!: $regex</start>
        q!: $regex<start>
        a: Салам Алейкум, я бот Максат и я помогу тебе найти квартиру по твоим настройкам самым первым.
        buttons:
            "Создать фильтр" -> /CreateOrUpdateUser
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
        
    state: HttpError
        a: Простите наши сервера не работают, можете пока отдохнуть а мы уже во всю работаем над их восстановлением


    state: CreateOrUpdateUser
        a: Отправь мне ссылку поиска со всеми включенными фильтрами, которую я смогу парсить и отправлять тебе все свежие кварьтры
        a: >Account id: {{$request.accountId}}
           > Channel user id: {{$request.channelUserId}}
           > User From data: id: {{$request.userFrom.id}} First name: {{$request.userFrom.FirstName}} Last name:  {{$request.userFrom.LastName}}
        HttpRequest: 
            url = https://test-kz-real-estate.free.beeceptor.com
            method = POST
            dataType = application/json
            body = {
                "account_id": "{{$request.accountId}}",
                "user_chat_id": "{{$request.userFrom.id}}"
                }
            headers = [{"name":"content-type","value":"application\/json"}]
            vars = [{"name":"","value":""}]
            errorState = /
        buttons:
            "Залупа" -> ./createFilter

        state: createFilter
            q: * (да/давай*/отлично) *
            q: * $filterURL *
            q: * $filterURL * || fromState = "/CreateOrUpdateUser"
            a: Отлично, мы записали твои предпочтения, я уведомлю тебя сразу как найду подходящие объявления!
            HttpRequest: 
                url = https://test-kz-real-estate.free.beeceptor.com
                method = POST
                dataType = application/json
                body = {
                    "account_id": {{$request.accountId}},
                    "user_chat_id": {{$request.userFrom.id}},
                    "first_name": {{$request.userFrom.FirstName}},
                    "last_name": {{$request.userFrom.LastName}},
                    "filters_url": {{$request.rawRequest}}
                    }
                headers = [{"name":"content-type","value":"application\/json"}]
                vars = [{"name":"","value":""}]


        state: Anything
            q: yes
            q: $regex<yes>
            q: $regex<yes> || fromState = "/CreateOrUpdateUser"
            a: Хуль ты не работаешь
        
        state: WrongUrl
            event: noMatch
            a: Ты втираешь мне какую-то дичь, это не ссылка поиска с сайта krisha.kz
            buttons:
                "Попробовать еще раз" -> /CreateOrUpdateUser
                "В начало" -> /Start
            