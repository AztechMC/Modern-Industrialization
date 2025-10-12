/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package aztech.modern_industrialization.pipes.api;

/**
 * The type of an endpoint.
 */
public enum PipeEndpointType {
    /**
     * The endpoint of this connection is another pipe.
     */
    PIPE(0),
    /**
     * The endpoint of this connection is not another pipe, but there is no display
     * difference except the shape.
     */
    BLOCK(1),
    /**
     * The endpoint of this connection is not another pipe, and the endpoint should
     * be displayed as inserting.
     */
    BLOCK_IN(2),
    /**
     * The endpoint of this connection is not another pipe, and the endpoint should
     * be displayed as inserting and extracting.
     */
    BLOCK_IN_OUT(3),
    /**
     * The endpoint of this connection is not another pipe, and the endpoint should
     * be displayed as extracting.
     */
    BLOCK_OUT(4),;

    private final int id;

    PipeEndpointType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PipeEndpointType byId(int id) {
        if (id == 0)
            return PIPE;
        else if (id == 1)
            return BLOCK;
        else if (id == 2)
            return BLOCK_IN;
        else if (id == 3)
            return BLOCK_IN_OUT;
        else if (id == 4)
            return BLOCK_OUT;
        else
            return null;
    }
}
