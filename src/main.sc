require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
require: localPatterns.sc




theme: /

    state: Start
        q!: $regex</start>
        q!: $regex<start>
        script:
            $jsapi.startSession();
            # $session.url = "https://test-kz-real-estate.free.beeceptor.com";
            # $session.url = "http://demo1759473.mockable.io/";
            $session.url = "http://84.201.152.243:8088";
            $session.checkUserEndpoint = "/filters/is-exist";
            $session.addFilterEndpoint = "/filters/add";
            $session.addUserEndpoint = "/users/add";
        a: Салам Алейкум, я бот Максат и я помогу тебе найти квартиру по твоим настройкам самым первым.
        go!: /CheckUser
    
    state: CheckUser
        q: CheckUser
        script:
            $session.requesURL = $session.url + $session.checkUserEndpoint + "?user_chat_id=" + $request.userFrom.id;
            log($session.requesURL);
        HttpRequest: 
            url = {{$session.requesURL}}
            method = GET
            vars =[{"name": "userExists", "value": "$httpResponse.user_exists"}, {"name": "filterExists", "value": "$httpResponse.filter_exists"}]
            errorState = /HttpError
            okState = ./UserExistanceHandler
        
        state: UserExistanceHandler
            script:
                log("User exists: " + $session.userExists)
            if: $session.userExists
                go!: /FiltersMenu
            else:
                go!: /CreateUser
                
        state: FilterExistanceHandler
            script:
                log("Filter exists: " + $session.filterExists)
            if: $session.filterExists
                go: /CahngeFilter
            else:
                go: /CreateUser
        
    state: CreateUser
        script:
            $session.requesURL = $session.url + $session.addUserEndpoint;
            log("User url: " + $session.requesURL)
        HttpRequest: 
            url = {{$session.requesURL}}
            method = POST
            body = {"account_id": "{{$request.accountId}}", "user_chat_id": "{{$request.userFrom.id}}"}
            errorState = /HttpError
            okState = /FiltersMenu
            
    state: FiltersMenu
        script:
            log("Filter exists: " + $session.filterExists)
        if: $session.filterExists
            go!: ./CahngeFilter
        else:
            go!: ./CreateFilter
        
        state: CahngeFilter
        a: У тебя уже есть активный фильтр, ты можешь его поменять или 
            отписаться, выбери действие
        buttons:
            "Создать новый" -> /CreateOrUpdateFilter
            "Отписаться" -> /DeleteFilter
            "На главную" -> /Start
    
        state: CreateFilter
            a: Как я вижу у тебя еще нет активного фильтра, хочешь создать новый?
            buttons:
                "Создать фильтр" -> /CreateOrUpdateFilter
                "На главную" -> /Start
        
    state: Reset
        q!: $regex</?reset>
        go!: /Start
        
        
    state: HttpError
        a: Простите наши сервера не работают, можете пока отдохнуть а мы уже во 
            всю работаем над их восстановлением. Попробуйте позже.
        go: /

    state: DeleteFilter
        a: NotImplemented TBD

    state: CreateOrUpdateFilter
        a: Отправь мне ссылку поиска со всеми включенными фильтрами, которую я смогу парсить и отправлять тебе все свежие кварьтры
        a: >Account id: {{$request.accountId}}
           > Channel user id: {{$request.channelUserId}}
           > User From data: id: {{$request.userFrom.id}} First name: {{$request.userFrom.FirstName}} Last name:  {{$request.userFrom.LastName}}
        # buttons:
        #     "На главную" -> ./CreateFilter
            
        
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
            script:
                $session.requesURL = $session.url + $session.addFilterEndpoint;
                log("Add filter url:" + $session.requesURL)
            HttpRequest: 
                url = {{$session.requesURL}}
                method = POST
                errorState = /HttpError
                okState = ./PlaceHolder
                body = {
                    "user_chat_id": "{{$request.userFrom.id}}", 
                    "filters_url": "{{$request.query}}"
                    }
                
            
    state: CatchAll || noContext = true
        event: noMatch
        a: Я не понял. Вы сказали: {{$request.query}}