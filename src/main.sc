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
    
    state: CheckUser
        q: CheckUser
        a: {{$env}}
        script:
            log(toPrettyString($env))
            log($env.CRUD_URL + "?name1=" + $request.userFrom.id)
        HttpRequest: 
            url = $env.CRUD_URL?name1=value1&name2=value2
            method = GET
            errorState = /HttpError
            okState = /CreateOrUpdateUser

        
    state: CreateUser
        HttpRequest: 
            url = $env.CRUD_URL
            method = POST
            dataType = application/json
            headers = [{"name":"content-type","value":"application\/json"}]
            errorState = /HttpError
            okState = ./PlaceHolder
            body = {
                "account_id": "{{$request.accountId}}",
                "user_chat_id": "{{$request.userFrom.id}}"
                }
    
    state: CheckFilter
        
        
        
        
    state: Reset
        q!: reset
        q!: Отмена
        q!: $regex</?reset>
        go!: /Start

    state: Hello
        intent!: /привет
        a: Привет привет
        

    state: Bye
        intent!: /пока
        a: Пока пока

    state: CatchAll || noContext = true
        event: noMatch
        a: Я не понял. Вы сказали: {{$request.query}}
        
        
    state: HttpError
        a: Простите наши сервера не работают, можете пока отдохнуть а мы уже во 
            всю работаем над их восстановлением. Попробуйте позже.
        go: /


    state: CreateOrUpdateUser
        a: Отправь мне ссылку поиска со всеми включенными фильтрами, которую я смогу парсить и отправлять тебе все свежие кварьтры
        a: >Account id: {{$request.accountId}}
           > Channel user id: {{$request.channelUserId}}
           > User From data: id: {{$request.userFrom.id}} First name: {{$request.userFrom.FirstName}} Last name:  {{$request.userFrom.LastName}}
        buttons:
            "На главную" -> ./CreateFilter
            
        state: PlaceHolder
        
            state: WrongUrl
                event: noMatch
                event: noMatch || fromState = "/CreateOrUpdateUser/PlaceHolder"
                a: Ты втираешь мне какую-то дичь, это не ссылка поиска с сайта krisha.kz
                buttons:
                    "Попробовать еще раз" -> /CreateOrUpdateUser
                    "В начало" -> /Start
    
            state: CreateFilter
                q: * $filterURL *
                q: * $filterURL * || fromState = "/CreateOrUpdateUser", onlyThisState = true
                a: Отлично, мы записали твои предпочтения, я уведомлю тебя сразу как найду подходящие объявления!
                # HttpRequest: 
                #     url = $env.CRUD_URL
                #     method = POST
                #     dataType = application/json
                #     headers = [{"name":"content-type","value":"application\/json"}]
                #     body = {
                #         "account_id": {{$request.accountId}},
                #         "user_chat_id": {{$request.userFrom.id}},
                #         "first_name": {{$request.userFrom.FirstName}},
                #         "last_name": {{$request.userFrom.LastName}},
                #         "filters_url": {{$request.rawRequest}}
                #     }
                    # errorState = /HttpError
                    # okState = ./PlaceHolder
        
    
            
    state: MorningExercise
        q!: exercise
        a: Do you do morning exercise?

        state: EveryDay
            q: * (yes/ Yeap) *
            a: Do you do it every day?

            state: Yes
                q: * (yes/ Yeap) *
                a: Good!

            state: No
                q: * (No/no) *
                a: Morning exercise should become your habit!

        state: No
            q: * (No/no) *
            a: Morning exercise helps your body and brain, try working on yourself!