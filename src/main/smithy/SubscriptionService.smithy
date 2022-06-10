namespace smithy4s.subscriptions

use smithy4s.api#simpleRestJson

@simpleRestJson
service SubscriptionHttpService {
    version: "1.0.0",
    operations: [Get]
}

@http(method: "GET", uri: "/subscriptions/{userid}", code: 200)
operation Get {
    input: SubscriptionsByUserRequest,
    output: SubscriptionsByUserResponse
}

structure SubscriptionsByUserRequest {
    @httpLabel
    @required
    userid: String
}

structure SubscriptionsByUserResponse {
    @required
    subscriptions: SubscriptionList
}

list SubscriptionList {
    member: Subscription
}

structure Subscription {
    @required
    organization: String,
    @required
    repository: String,
    subscribedAt: Timestamp
}


