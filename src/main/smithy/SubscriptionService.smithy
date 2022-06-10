namespace smithy4s.subscriptions

use smithy4s.api#simpleRestJson

@simpleRestJson
service SubscriptionHttpService {
    version: "1.0.0",
    operations: [Get, Create]
}

@http(method: "GET", uri: "/subscriptions/{userid}", code: 200)
operation Get {
    input: SubscriptionsByUserRequest,
    output: SubscriptionsByUserResponse
}

@idempotent
@http(method: "PUT", uri: "/subscriptions/{userid}", code: 201)
operation Create {
    input: SubscriptionsByUserRequest
}

structure CreateSubcriptionRequest {
    @httpLabel
    @required
    userid: String,
    @required
    subscription: Subscription
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


