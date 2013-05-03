
/**
 *Copyright (c) 2012, IMPACT working group at the Centrum für Informations- und Sprachverarbeitung, University of Munich.
 *All rights reserved.

 *Redistribution and use in source and binary forms, with or without
 *modification, are permitted provided that the following conditions are met:

 *Redistributions of source code must retain the above copyright
 *notice, this list of conditions and the following disclaimer.
 *Redistributions in binary form must reproduce the above copyright
 *notice, this list of conditions and the following disclaimer in the
 *documentation and/or other materials provided with the distribution.

 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This file is part of the ocr-postcorrection tool developed
 * by the IMPACT working group at the Centrum für Informations- und Sprachverarbeitung, University of Munich.
 * For further information and contacts visit http://ocr.cis.uni-muenchen.de/
 * 
 * @author thorsten (thorsten.vobl@googlemail.com)
 */
/**
 * ProfilerWebServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:22:40 CEST)
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
            * auto generated Axis2 call back method for createAccount method
            * override this method for handling normal response from createAccount operation
            */
           public void receiveResultcreateAccount(
                    cis.profiler.client.ProfilerWebServiceStub.CreateAccountResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from createAccount operation
           */
            public void receiveErrorcreateAccount(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getSimpleConfigurations method
            * override this method for handling normal response from getSimpleConfigurations operation
            */
           public void receiveResultgetSimpleConfigurations(
                    cis.profiler.client.ProfilerWebServiceStub.GetSimpleConfigurationsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getSimpleConfigurations operation
           */
            public void receiveErrorgetSimpleConfigurations(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for validateEmail method
            * override this method for handling normal response from validateEmail operation
            */
           public void receiveResultvalidateEmail(
                    cis.profiler.client.ProfilerWebServiceStub.ValidateEmailResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from validateEmail operation
           */
            public void receiveErrorvalidateEmail(java.lang.Exception e) {
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
            * auto generated Axis2 call back method for resendID method
            * override this method for handling normal response from resendID operation
            */
           public void receiveResultresendID(
                    cis.profiler.client.ProfilerWebServiceStub.ResendIDResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from resendID operation
           */
            public void receiveErrorresendID(java.lang.Exception e) {
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
                
           /**
            * auto generated Axis2 call back method for getTransactions method
            * override this method for handling normal response from getTransactions operation
            */
           public void receiveResultgetTransactions(
                    cis.profiler.client.ProfilerWebServiceStub.GetTransactionsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getTransactions operation
           */
            public void receiveErrorgetTransactions(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for checkQuota method
            * override this method for handling normal response from checkQuota operation
            */
           public void receiveResultcheckQuota(
                    cis.profiler.client.ProfilerWebServiceStub.CheckQuotaResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from checkQuota operation
           */
            public void receiveErrorcheckQuota(java.lang.Exception e) {
            }
                


    }
    