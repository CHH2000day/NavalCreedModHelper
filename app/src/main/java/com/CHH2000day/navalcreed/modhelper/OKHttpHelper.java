package com.CHH2000day.navalcreed.modhelper;
import okhttp3.*;
import okhttp3.tls.*;
import java.net.*;
import com.orhanobut.logger.*;

public class OKHttpHelper
{ 
	private static OkHttpClient client;
	
	public synchronized static OkHttpClient getClient(){
		try
		{
			if (client == null)init();
		}
		catch (UnknownHostException e)
		{Logger.e(e,"Failed to init Okhttp");}
		return client;
	}
	private static void init() throws UnknownHostException{
		HeldCertificate rootCertificate = new HeldCertificate.Builder()
			.certificateAuthority(1)
			.build();

		HeldCertificate intermediateCertificate = new HeldCertificate.Builder()
			.certificateAuthority(0)
			.signedBy(rootCertificate)
			.build();

		String localhost = InetAddress.getByName("localhost").getCanonicalHostName();
		HeldCertificate serverCertificate = new HeldCertificate.Builder()
			.addSubjectAlternativeName(localhost)
			.signedBy(intermediateCertificate)
			.build();
		HandshakeCertificates serverHandshakeCertificates = new HandshakeCertificates.Builder()
			.heldCertificate(serverCertificate, intermediateCertificate.certificate())
			.build();
		HandshakeCertificates clientCertificates = new HandshakeCertificates.Builder()
			.addTrustedCertificate(rootCertificate.certificate())
			.build();
		client = new OkHttpClient.Builder()
			.sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager())
			.build();
	}
}
