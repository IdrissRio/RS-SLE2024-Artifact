
/* Copyright (c) 2023, Idriss Riouak <idriss.riouak@cs.lth.se>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.extendj.flow.utils;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.extendj.ast.ASTNode;
import org.extendj.ast.ASTState;
import org.extendj.ast.MethodDecl;

public class AttributeTracer implements ASTState.Trace.Receiver {
  public Map<String, Integer> computeBegin =
      new LinkedHashMap<String, Integer>();
  public Map<String, Integer> case1Begin = new LinkedHashMap<String, Integer>();
  public Map<String, Integer> case2Begin = new LinkedHashMap<String, Integer>();

  public void writeMapToFile(String filename, int iteration) {
    writeMapToFilePrivate(filename + iteration + "computeBegin.txt",
                          computeBegin);
    // writeMapToFilePrivate(filename + iteration + "case1Begin.txt",
    // case1Begin); writeMapToFilePrivate(filename + iteration +
    // "case2Begin.txt", case2Begin);
    // System.err.println("Wrote attribute traces to " + filename + iteration +
    //                    "computebegin.txt");
  }

  private void writeMapToFilePrivate(String fileName,
                                     Map<String, Integer> map) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
      for (Map.Entry<String, Integer> entry : map.entrySet()) {
        writer.write(entry.getKey() + ": " + entry.getValue());
        writer.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void accept(ASTState.Trace.Event event, ASTNode node, String attribute,
                     Object params, Object value) {

    String attrName = attribute;
    attrName =
        attribute.substring(attribute.indexOf('.') + 1, attribute.length());

    switch (event) {
    case COMPUTE_BEGIN: {
      //   if (node instanceof MethodDecl) {
      computeBegin.put(attrName, computeBegin.getOrDefault(attrName, 0) + 1);
      //   }
      break;
    }
    case CIRCULAR_CASE1_START: {
      //   if (node instanceof MethodDecl) {
      case1Begin.put(attrName, case1Begin.getOrDefault(attrName, 0) + 1);
      //   }
      break;
    }
    case CIRCULAR_CASE2_START: {
      //   if (node instanceof MethodDecl) {
      case2Begin.put(attrName, case2Begin.getOrDefault(attrName, 0) + 1);
      //   }
      break;
    }
    default: {
      // System.out.println("Event: " + event);
      break;
    }
    }
  }
}