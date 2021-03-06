/*
 * Copyright 2013 Robert von Burg <eitch@eitchnet.ch>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package li.strolch.privilege.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKeyFactory;

import li.strolch.privilege.handler.DefaultEncryptionHandler;
import li.strolch.utils.helper.StringHelper;

/**
 * <p>
 * Simple main class which can be used to create a hash from a password which the user must type in at the command line
 * </p>
 * 
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public class PasswordCreator {

	/**
	 * @param args
	 *            the args from the command line, NOT USED
	 * @throws Exception
	 *             thrown if anything goes wrong
	 */
	@SuppressWarnings("nls")
	public static void main(String[] args) throws Exception {

		while (true) {

			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

			String hashAlgorithm = null;
			while (hashAlgorithm == null) {
				System.out.print("Hash Algorithm [PBKDF2WithHmacSHA512]: ");
				String readLine = r.readLine().trim();

				if (readLine.isEmpty()) {
					hashAlgorithm = "PBKDF2WithHmacSHA512";
				} else {

					try {
						SecretKeyFactory.getInstance(readLine);
						hashAlgorithm = readLine;
					} catch (Exception e) {
						System.err.println(e.getLocalizedMessage());
						hashAlgorithm = null;
					}
				}
			}

			int iterations = -1;
			while (iterations == -1) {
				System.out.print("Hash iterations [10000]: ");
				String readLine = r.readLine().trim();

				if (readLine.isEmpty()) {
					iterations = 10000;
				} else {

					try {
						iterations = Integer.parseInt(readLine);
					} catch (Exception e) {
						System.err.println(e.getLocalizedMessage());
						iterations = -1;
					}
				}
			}

			int keyLength = -1;
			while (keyLength == -1) {
				System.out.print("Hash keyLength [256]: ");
				String readLine = r.readLine().trim();

				if (readLine.isEmpty()) {
					keyLength = 256;
				} else {

					try {
						keyLength = Integer.parseInt(readLine);
						if (keyLength <= 0)
							throw new IllegalArgumentException("KeyLength must be > 0");
					} catch (Exception e) {
						System.err.println(e.getLocalizedMessage());
						keyLength = -1;
					}
				}
			}

			Map<String, String> parameterMap = new HashMap<>();
			parameterMap.put(XmlConstants.XML_PARAM_HASH_ALGORITHM, hashAlgorithm);
			parameterMap.put(XmlConstants.XML_PARAM_HASH_ITERATIONS, "" + iterations);
			parameterMap.put(XmlConstants.XML_PARAM_HASH_KEY_LENGTH, "" + keyLength);

			DefaultEncryptionHandler encryptionHandler = new DefaultEncryptionHandler();
			encryptionHandler.initialize(parameterMap);

			System.out.print("Password: ");
			char[] password = r.readLine().trim().toCharArray();
			System.out.print("Salt [random]: ");
			String saltTemp = r.readLine().trim();
			if (saltTemp.isEmpty()) {
				saltTemp = encryptionHandler.nextToken();
			}
			String saltS = StringHelper.getHexString(saltTemp.getBytes());
			byte[] salt = StringHelper.fromHexString(saltS);

			byte[] passwordHash = encryptionHandler.hashPassword(password, salt);
			String passwordHashS = StringHelper.getHexString(passwordHash);
			System.out.println("Hash is: " + passwordHashS);
			System.out.println("Salt is: " + saltS);
			System.out.println();

			System.out.println(XmlConstants.XML_ATTR_PASSWORD + "=\"" + passwordHashS + "\" "
					+ XmlConstants.XML_ATTR_SALT + "=\"" + saltS + "\"");
			System.out.println();
		}
	}
}
