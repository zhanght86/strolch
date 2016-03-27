/**
 * Created by eitch on 2015-09-04
 */

/*
 * Define the main namespace
 */
if (typeof strolch == 'undefined') {
	strolch = {};
}

strolch.const = {
    url_base: 'planningwebapp',
    urls: {
        auth: 'rest/strolch/authentication',
        version: 'rest/version',
    },

    auth_token: 'auth_token',
    session_data: 'session_data'
};