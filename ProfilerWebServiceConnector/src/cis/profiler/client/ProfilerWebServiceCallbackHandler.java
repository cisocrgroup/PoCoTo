
/**
 * ProfilerWebServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */

    package cis.profiler.client;

    /**
     *  ProfilerWebServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class ProfilerWebServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public ProfilerWebServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public ProfilerWebServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for simpleEnrich method
            * override this method for handling normal response from simpleEnrich operation
            */
           public void receiveResultsimpleEnrich(
                    cis.profiler.client.ProfilerWebServiceStub.SimpleEnrichResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from simpleEnrich operation
           */
            public void receiveErrorsimpleEnrich(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for startSession method
            * override this method for handling normal response from startSession operation
            */
           public void receiveResultstartSession(
                    cis.profiler.client.ProfilerWebServiceStub.StartSessionResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from startSession operation
           */
            public void receiveErrorstartSession(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getConfigurations method
            * override this method for handling normal response from getConfigurations operation
            */
           public void receiveResultgetConfigurations(
                    cis.profiler.client.ProfilerWebServiceStub.GetConfigurationsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getConfigurations operation
           */
            public void receiveErrorgetConfigurations(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for abortProfiling method
            * override this method for handling normal response from abortProfiling operation
            */
           public void receiveResultabortProfiling(
                    cis.profiler.client.ProfilerWebServiceStub.AbortProfilingResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from abortProfiling operation
           */
            public void receiveErrorabortProfiling(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getProfile method
            * override this method for handling normal response from getProfile operation
            */
           public void receiveResultgetProfile(
                    cis.profiler.client.ProfilerWebServiceStub.GetProfileResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getProfile operation
           */
            public void receiveErrorgetProfile(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getProfilingStatus method
            * override this method for handling normal response from getProfilingStatus operation
            */
           public void receiveResultgetProfilingStatus(
                    cis.profiler.client.ProfilerWebServiceStub.GetProfilingStatusResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getProfilingStatus operation
           */
            public void receiveErrorgetProfilingStatus(java.lang.Exception e) {
            }
                


    }
    