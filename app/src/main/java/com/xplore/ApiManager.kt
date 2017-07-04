package com.xplore

import android.app.Activity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient

/**
 * Created by Nikaoto on 7/4/2017.
 *
 * აღწერა:
 * ეს არის სინგლტონ კლასი (ობიექტი). ამარტივებს სხვა კლასებისთვის API კლიენტების აშენებას. ApiManager
 * იძლევა საშუალებას რომ მოვიპოვოთ რაიმე სერვისის (Facebook, Google...) API კლიენტი პირდაპირ
 * getServiceApiClient() ფუნქციით (სადაც Service ჩანაცვლებულია სახელით)
 *
 * Description:
 * This is a singleton class (object), which simplifies API client creation for other classes.
 * It allows us to get any API client of a service with the getServiceApiClient() function, where
 * "Service" is the name of the API provider.
 *
 */

object ApiManager {

    fun getGoogleAuthApiClient(activity: Activity) =
            buildGoogleApiClient(activity, buildGoogleSignInOptions(activity))

    //fun getFacebookApiClient

    //Google Auth Api Client//
    fun buildGoogleApiClient(activity: Activity, gso: GoogleSignInOptions) =
            GoogleApiClient.Builder(activity.applicationContext)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()

    fun buildGoogleSignInOptions(activity: Activity): GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(activity.resources.getString(R.string.default_web_client_id))
                        .requestEmail().build()
    //

    //Facebook Api Client//

    //
}