'use client'

import {useState, useEffect} from 'react'
import {Button} from "@/components/ui/button"
import {Input} from "@/components/ui/input"
import {Label} from "@/components/ui/label"
import {RadioGroup, RadioGroupItem} from "@/components/ui/radio-group"
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select"
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card"

export function OtpGenerator() {
    const [secretKey, setSecretKey] = useState('')
    const [algorithm, setAlgorithm] = useState('SHA-1')
    const [otpType, setOtpType] = useState('HOTP')
    const [format, setFormat] = useState('Base32')
    const [counter, setCounter] = useState(0)
    const [digits, setDigits] = useState(6)
    const [timeStep, setTimeStep] = useState(30)
    const [generatedOTP, setGeneratedOTP] = useState('')
    const [error, setError] = useState('')

    const generateSecretKey = () => {
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ23456789'
        let result = ''
        for (let i = 0; i < 32; i++) {
            result += characters.charAt(Math.floor(Math.random() * characters.length))
        }
        setSecretKey(result)
    }

    const generateOTP = async () => {
        // Simulating API call
        const host = window.location.protocol + "//" + window.location.host;
        const response = await fetch(host + '/api/v1/generate-otp', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                secretKey,
                format,
                algorithm,
                digits,
                otpType,
                counter: otpType === 'HOTP' ? counter : undefined,
                timeStep: otpType === 'TOTP' ? timeStep : undefined,
            }),
        })

        try {
            const data = await response.clone().json()
            setGeneratedOTP(data)
            setError('')
        } catch {
            const err = await response.clone().text()
            setError(err)
        }

    }

    useEffect(() => {
        generateOTP()
    }, [secretKey, algorithm, otpType, format, digits, counter, timeStep, generateOTP])

    // if TOTP is selected, regenerate OTP every timeStep seconds
    useEffect(() => {
        if (otpType === 'TOTP' && timeStep >= 1) {
            const interval = setInterval(() => {
                generateOTP()
            }, timeStep * 1000)
            return () => clearInterval(interval)
        }
    }, [otpType, timeStep, generateOTP])

    return (
        <Card className="w-full max-w-2xl mx-auto">
            <CardHeader>
                <CardTitle className="text-2xl font-bold">OTP Generator</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
                <div className="space-y-2">
                    <Label htmlFor="secretKey">Secret Key</Label>
                    <div className="flex space-x-2">
                        <Input
                            id="secretKey"
                            value={secretKey}
                            onChange={(e) => setSecretKey(e.target.value)}
                            placeholder="Enter secret key"
                            className="flex-grow"
                        />
                        <Button onClick={generateSecretKey}>Auto-generate</Button>
                    </div>
                </div>

                <div className="space-y-2">
                    <Label htmlFor="format">Secret Key Format</Label>
                    <Select value={format} onValueChange={setFormat}>
                        <SelectTrigger id="format">
                            <SelectValue placeholder="Select format"/>
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="UTF-8">UTF-8</SelectItem>
                            <SelectItem value="Hex">Hex</SelectItem>
                            <SelectItem value="Base32">Base32</SelectItem>
                            <SelectItem value="Base64">Base64</SelectItem>
                        </SelectContent>
                    </Select>
                </div>

                <div className="space-y-2">
                    <Label htmlFor="algorithm">Algorithm</Label>
                    <Select value={algorithm} onValueChange={setAlgorithm}>
                        <SelectTrigger id="algorithm">
                            <SelectValue placeholder="Select algorithm"/>
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="MD5">MD5</SelectItem>
                            <SelectItem value="SHA-1">SHA-1</SelectItem>
                            <SelectItem value="SHA-256">SHA-256</SelectItem>
                            <SelectItem value="SHA-512">SHA-512</SelectItem>
                            <SelectItem value="SHA3-256">SHA3-256</SelectItem>
                            <SelectItem value="SHA3-512">SHA3-512</SelectItem>
                        </SelectContent>
                    </Select>
                </div>

                <div className="space-y-2">
                    <Label htmlFor="digits">Digits</Label>
                    <Input
                        id="digits"
                        type="number"
                        value={digits}
                        onChange={(e) => setDigits(parseInt(e.target.value))}
                        min={0}
                    />
                </div>

                <div className="space-y-2">
                    <Label>OTP Type</Label>
                    <RadioGroup value={otpType} onValueChange={setOtpType} className="flex space-x-4">
                        <div className="flex items-center space-x-2">
                            <RadioGroupItem value="HOTP" id="hotp"/>
                            <Label htmlFor="hotp">HOTP</Label>
                        </div>
                        <div className="flex items-center space-x-2">
                            <RadioGroupItem value="TOTP" id="totp"/>
                            <Label htmlFor="totp">TOTP</Label>
                        </div>
                    </RadioGroup>
                </div>

                {otpType === 'HOTP' && (
                    <div className="space-y-2">
                        <Label htmlFor="counter">Counter</Label>
                        <Input
                            id="counter"
                            type="number"
                            value={counter}
                            onChange={(e) => setCounter(parseInt(e.target.value))}
                            min={0}
                        />
                    </div>
                )}

                {otpType === 'TOTP' && (
                    <div className="space-y-2">
                        <Label htmlFor="timeChunk">Time Chunk</Label>
                        <Input
                            id="timeChunk"
                            type="number"
                            value={timeStep}
                            onChange={(e) => setTimeStep(parseInt(e.target.value))}
                            min={1}
                        />
                    </div>
                )}

                <Button onClick={generateOTP} className="w-full">Generate</Button>

                {generatedOTP && (
                    <div className="mt-4 p-4 bg-secondary rounded-md">
                        <Label>Generated OTP:</Label>
                        <div
                            className="text-3xl font-mono font-bold text-center">{generatedOTP.toString().padStart(digits, '0')}</div>
                    </div>
                )}

                {error && (
                    <div className="mt-4 p-4 bg-red-100 text-red-600 rounded-md">
                        {error}
                    </div>
                )}
            </CardContent>
        </Card>
    )
}
