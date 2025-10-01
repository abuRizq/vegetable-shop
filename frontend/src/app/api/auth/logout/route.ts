
import { cookies } from 'next/headers'
import { NextResponse } from 'next/server'

const POST = async () => {
    (await cookies()).set(
        {
            name: 'at', value: '', httpOnly: true,
            secure: process.env.NODE_ENV === 'production',
            sameSite: 'lax', path: '/', maxAge: 0
        })
    return NextResponse.json({ success: true })
}
export { POST };